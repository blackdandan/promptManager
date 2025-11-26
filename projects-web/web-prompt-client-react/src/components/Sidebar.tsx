import { Prompt } from '../App';
import { Button } from './ui/button';
import { ScrollArea } from './ui/scroll-area';
import { Separator } from './ui/separator';
import { ContextMenu, ContextMenuContent, ContextMenuItem, ContextMenuTrigger } from './ui/context-menu';
import { 
  Folder, 
  FolderOpen, 
  Star, 
  Search, 
 Plus, 
  Settings, 
  ChevronRight,
  ChevronDown,
  Home,
  Clock,
  MoreHorizontal,
  Edit,
  Trash2,
  GripVertical
} from 'lucide-react';
import { useState } from 'react';
import { toast } from 'sonner';
import type { Folder as ApiFolder, PromptStats } from '../types/api';

type SidebarProps = {
  prompts: Prompt[];
  folders: ApiFolder[];
  stats?: PromptStats | null;
  currentView: 'main' | 'search' | 'profile';
  selectedFolder: string | null;
  filterType: 'all' | 'favorites' | 'recent';
  onViewChange: (view: 'main' | 'search' | 'profile') => void;
  onFolderSelect: (folder: string | null) => void;
  onFilterChange: (type: 'all' | 'favorites' | 'recent') => void;
  onCreateClick: () => void;
  onCreateFolder?: (folderName: string, parentId?: string) => void;
  onEditFolder?: (folderId: string, newName: string) => void;
  onDeleteFolder?: (folderId: string) => void;
  onReorderFolder?: (folderId: string, newOrder: number) => void;
};

export function Sidebar({ 
  prompts, 
  folders,
  stats,
  currentView, 
  selectedFolder, 
  filterType,
  onViewChange, 
  onFolderSelect,
  onFilterChange,
  onCreateClick,
  onCreateFolder,
  onEditFolder,
  onDeleteFolder,
  onReorderFolder
}: SidebarProps) {
  const [expandedFolders, setExpandedFolders] = useState<Set<string>>(new Set());
  const [draggedFolder, setDraggedFolder] = useState<{id: string, index: number} | null>(null);

  const handleEditFolder = (folderId: string, currentName: string) => {
    const newName = prompt('请输入新的文件夹名称:', currentName);
    if (newName && newName.trim() && newName.trim() !== currentName) {
      if (onEditFolder) {
        onEditFolder(folderId, newName.trim());
      }
    }
  };

  const handleDeleteFolder = (folderId: string, folderName: string) => {
    const confirmed = confirm(`确定要删除文件夹 "${folderName}" 吗？\n\n注意：只有空文件夹才能被删除。`);
    if (confirmed) {
      if (onDeleteFolder) {
        onDeleteFolder(folderId);
      }
    }
  };

  const handleCreateSubFolder = (parentId: string, parentName: string, parentLevel: number = 0) => {
    if (parentLevel >= 2) {
      toast.error('最多创建3级目录');
      return;
    }
    const subFolderName = prompt(`请输入 "${parentName}" 的子文件夹名称:`);
    if (subFolderName && subFolderName.trim()) {
      if (onCreateFolder) {
        onCreateFolder(subFolderName.trim(), parentId);
      }
    }
  };

  // 构建文件夹树
  const buildFolderTree = () => {
    const folderMap = new Map<string, { name: string; path: string; level: number; children: string[]; count: number }>();
    const rootFolders: string[] = [];

    // 使用API返回的文件夹数据
    if (folders.length > 0) {
      // 首先创建所有文件夹的映射
      folders.forEach(folder => {
        folderMap.set(folder.id, {
          name: folder.name,
          path: folder.id,
          level: 0, // 会在后续计算中更新
          children: [],
          count: folder.promptCount || 0,
        });
      });

      // 计算每个文件夹的层级并建立父子关系
      folders.forEach(folder => {
        if (folder.parentId) {
          // 这是一个子文件夹，找到它的父文件夹
          const parentFolder = folderMap.get(folder.parentId);
          if (parentFolder) {
            // 添加到父文件夹的children数组
            parentFolder.children.push(folder.id);
            // 计算当前文件夹的层级（比父文件夹层级+1）
            const parentLevel = parentFolder.level;
            const currentFolder = folderMap.get(folder.id);
            if (currentFolder) {
              currentFolder.level = parentLevel + 1;
            }
          }
        } else {
          // 这是一个根文件夹
          rootFolders.push(folder.id);
        }
      });

      // 确保所有文件夹的层级都被正确计算
      const calculateLevels = (folderId: string, currentLevel: number) => {
        const folder = folderMap.get(folderId);
        if (folder) {
          folder.level = currentLevel;
          // 递归计算子文件夹的层级
          folder.children.forEach(childId => {
            calculateLevels(childId, currentLevel + 1);
          });
        }
      };

      rootFolders.forEach(rootId => {
        calculateLevels(rootId, 0);
      });
    } else {
      // 回退到从prompts中构建文件夹树
      const folderCounts = new Map<string, number>();
      prompts.forEach(prompt => {
        if (prompt.folder) {
          const parts = prompt.folder.split('/');
          let currentPath = '';
          parts.forEach((part, index) => {
            if (index < 3) {
              currentPath = currentPath ? `${currentPath}/${part}` : part;
              folderCounts.set(currentPath, (folderCounts.get(currentPath) || 0) + 1);
            }
          });
        }
      });

      prompts.forEach(prompt => {
        if (!prompt.folder) return;
        
        const parts = prompt.folder.split('/').slice(0, 3);
        let currentPath = '';
        
        parts.forEach((part, level) => {
          const parentPath = currentPath;
          currentPath = currentPath ? `${currentPath}/${part}` : part;
          
          if (!folderMap.has(currentPath)) {
            folderMap.set(currentPath, {
              name: part,
              path: currentPath,
              level: level,
              children: [],
              count: folderCounts.get(currentPath) || 0,
            });
            
            if (level === 0) {
              rootFolders.push(currentPath);
            } else {
              const parent = folderMap.get(parentPath);
              if (parent && !parent.children.includes(currentPath)) {
                parent.children.push(currentPath);
              }
            }
          }
        });
      });
    }

    return { folderMap, rootFolders: Array.from(new Set(rootFolders)) };
  };

  const toggleFolder = (path: string) => {
    const newExpanded = new Set(expandedFolders);
    if (newExpanded.has(path)) {
      newExpanded.delete(path);
    } else {
      newExpanded.add(path);
    }
    setExpandedFolders(newExpanded);
  };

  const renderFolderNode = (path: string, folderMap: Map<string, any>, parentId?: string) => {
    const node = folderMap.get(path);
    if (!node) return null;

    const isExpanded = expandedFolders.has(node.path);
    const isSelected = selectedFolder === node.path;
    const hasChildren = node.children.length > 0;

    return (
      <ContextMenu>
        <ContextMenuTrigger>
          <div key={node.path}>
            <div
              className={`flex items-center gap-2 px-3 py-2 rounded-md cursor-pointer transition-colors group ${
                isSelected ? 'bg-blue-50 text-blue-700' : 'hover:bg-gray-100'
              }`}
              style={{ paddingLeft: `${12 + node.level * 16}px` }}
              onClick={() => onFolderSelect(node.path)}
              draggable
              onDragStart={(e) => {
                e.dataTransfer.setData('text/plain', node.path);
                // 获取同级兄弟节点
                const siblings = parentId ? 
                  (folderMap.get(parentId)?.children || rootFolders) : 
                  rootFolders;
                setDraggedFolder({id: node.path, index: siblings.indexOf(node.path)});
              }}
              onDragOver={(e) => {
                e.preventDefault();
              }}
              onDrop={(e) => {
                e.preventDefault();
                const draggedId = e.dataTransfer.getData('text/plain');
                if (draggedId && draggedId !== node.path) {
                  // 获取同级兄弟节点
                  const siblings = parentId ? 
                    (folderMap.get(parentId)?.children || rootFolders) : 
                    rootFolders;
                  const draggedIndex = siblings.indexOf(draggedId);
                  const targetIndex = siblings.indexOf(node.path);
                  if (draggedIndex !== -1 && targetIndex !== -1 && onReorderFolder) {
                    onReorderFolder(draggedId, targetIndex);
                  }
                }
                setDraggedFolder(null);
              }}
            >
              {hasChildren && node.level < 2 && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleFolder(node.path);
                  }}
                  className="flex-shrink-0 hover:bg-gray-200 rounded p-0.5"
                >
                  {isExpanded ? (
                    <ChevronDown className="w-3.5 h-3.5 text-gray-500" />
                  ) : (
                    <ChevronRight className="w-3.5 h-3.5 text-gray-500" />
                  )}
                </button>
              )}
              {(!hasChildren || node.level >= 2) && (
                <div className="w-4" />
              )}
              {isExpanded && hasChildren ? (
                <FolderOpen className={`w-4 h-4 ${isSelected ? 'text-blue-600' : 'text-gray-500'}`} />
              ) : (
                <Folder className={`w-4 h-4 ${isSelected ? 'text-blue-600' : 'text-gray-500'}`} />
              )}
              <span className="flex-1 text-sm truncate">{node.name}</span>
              <span className="text-xs text-gray-400">{node.count}</span>
              <GripVertical className="w-4 h-4 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
            {isExpanded && hasChildren && node.level < 2 && (
              <div className="ml-4"> {/* 为子文件夹添加左边距 */}
                {node.children.map((childPath: string) => renderFolderNode(childPath, folderMap, node.path))}
              </div>
            )}
          </div>
        </ContextMenuTrigger>
        <ContextMenuContent className="w-48">
          <ContextMenuItem onClick={() => handleEditFolder(node.path, node.name)}>
            <Edit className="w-4 h-4 mr-2" />
            重命名
          </ContextMenuItem>
          <ContextMenuItem onClick={() => handleCreateSubFolder(node.path, node.name, node.level)}>
            <Plus className="w-4 h-4 mr-2" />
            新建子文件夹
          </ContextMenuItem>
          <ContextMenuItem onClick={() => handleDeleteFolder(node.path, node.name)}>
            <Trash2 className="w-4 h-4 mr-2" />
            删除
          </ContextMenuItem>
        </ContextMenuContent>
      </ContextMenu>
    );
  };


  const { folderMap, rootFolders } = buildFolderTree();
  
  // 如果有后端统计数据则使用，否则（如游客模式）使用本地计算
  const totalCount = stats ? stats.totalPrompts : prompts.length;
  const favoriteCount = stats ? stats.favoritePrompts : prompts.filter(p => p.isFavorite).length;

  return (
    <div className="w-64 border-r bg-white flex flex-col h-screen">
      {/* Logo/Header */}
      <div className="p-4 border-b">
        <h1 className="text-xl font-semibold">Prompt Manager</h1>
      </div>

      {/* New Prompt Button */}
      <div className="p-4">
        <Button className="w-full" onClick={onCreateClick}>
          <Plus className="w-4 h-4 mr-2" />
          新建 Prompt
        </Button>
      </div>

      <Separator />

      {/* Navigation */}
      <ScrollArea className="flex-1">
        <div className="p-2 space-y-1">
          <Button
            variant={currentView === 'main' && !selectedFolder ? 'secondary' : 'ghost'}
            className="w-full justify-start"
            onClick={() => {
              onViewChange('main');
              onFolderSelect(null);
            }}
          >
            <Home className="w-4 h-4 mr-3" />
            全部 Prompts
            <span className="ml-auto text-xs text-gray-500">{totalCount}</span>
          </Button>

          <Button
            variant={filterType === 'favorites' ? 'secondary' : 'ghost'}
            className="w-full justify-start"
            onClick={() => onFilterChange('favorites')}
          >
            <Star className="w-4 h-4 mr-3" />
            收藏
            <span className="ml-auto text-xs text-gray-500">{favoriteCount}</span>
          </Button>

          <Button
            variant={filterType === 'recent' ? 'secondary' : 'ghost'}
            className="w-full justify-start"
            onClick={() => onFilterChange('recent')}
          >
            <Clock className="w-4 h-4 mr-3" />
            最近
          </Button>

          <Button
            variant={currentView === 'search' ? 'secondary' : 'ghost'}
            className="w-full justify-start"
            onClick={() => onViewChange('search')}
          >
            <Search className="w-4 h-4 mr-3" />
            搜索
          </Button>
        </div>

        <Separator className="my-2" />

        {/* Folders */}
        <div className="p-2">
          <div className="px-3 py-2 flex items-center justify-between group">
            <p className="text-xs text-gray-50 uppercase tracking-wide">文件夹</p>
            <button
              className="opacity-0 group-hover:opacity-100 transition-opacity p-1 hover:bg-gray-200 rounded"
              onClick={(e) => {
                e.stopPropagation();
                if (onCreateFolder) {
                  const folderName = prompt('请输入文件夹名称:');
                  if (folderName && folderName.trim()) {
                    onCreateFolder(folderName.trim(), undefined);
                  }
                }
              }}
            >
              <Plus className="w-3 h-3 text-gray-500" />
            </button>
          </div>

          {/* Default Folder Item */}
          <div
            className={`flex items-center gap-2 px-3 py-2 rounded-md cursor-pointer transition-colors mb-1 ${
              selectedFolder === "root" ? 'bg-blue-50 text-blue-700' : 'hover:bg-gray-100'
            }`}
            onClick={() => onFolderSelect("root")}
            style={{ paddingLeft: '12px' }}
          >
             <div className="w-4" />
             <Folder className={`w-4 h-4 ${selectedFolder === "root" ? 'text-blue-600' : 'text-gray-500'}`} />
             <span className="flex-1 text-sm truncate">默认文件夹</span>
          </div>
          
          {rootFolders.length > 0 ? (
            <div className="space-y-0.5">
              {rootFolders.map(path => renderFolderNode(path, folderMap))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-400">
              <Folder className="w-8 h-8 mx-auto mb-2 opacity-50" />
              <p className="text-xs">暂无文件夹</p>
            </div>
          )}
        </div>
      </ScrollArea>

      <Separator />

      {/* Settings */}
      <div className="p-2">
        <Button
          variant={currentView === 'profile' ? 'secondary' : 'ghost'}
          className="w-full justify-start"
          onClick={() => onViewChange('profile')}
        >
          <Settings className="w-4 h-4 mr-3" />
          设置
        </Button>
      </div>
    </div>
  );
}
