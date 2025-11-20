import { Prompt } from '../App';
import { Button } from './ui/button';
import { ScrollArea } from './ui/scroll-area';
import { Separator } from './ui/separator';
import { 
  Folder, 
  FolderOpen, 
  Star, 
  Search, 
  Plus, 
  Settings, 
  ChevronRight,
  ChevronDown,
  Home
} from 'lucide-react';
import { useState } from 'react';
import type { Folder as ApiFolder } from '../types/api';

type SidebarProps = {
  prompts: Prompt[];
  folders: ApiFolder[];
  currentView: 'main' | 'search' | 'profile';
  selectedFolder: string | null;
  onViewChange: (view: 'main' | 'search' | 'profile') => void;
  onFolderSelect: (folder: string | null) => void;
  onCreateClick: () => void;
};

export function Sidebar({ 
  prompts, 
  folders,
  currentView, 
  selectedFolder, 
  onViewChange, 
  onFolderSelect,
  onCreateClick 
}: SidebarProps) {
  const [expandedFolders, setExpandedFolders] = useState<Set<string>>(new Set());

  // 构建文件夹树
  const buildFolderTree = () => {
    const folderMap = new Map<string, { name: string; path: string; level: number; children: string[]; count: number }>();
    const rootFolders: string[] = [];

    // 使用API返回的文件夹数据
    if (folders.length > 0) {
      folders.forEach(folder => {
        folderMap.set(folder.id, {
          name: folder.name,
          path: folder.id,
          level: 0, // 简化处理，所有文件夹都作为根级
          children: [],
          count: folder.promptCount || 0,
        });
        rootFolders.push(folder.id);
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

  const renderFolderNode = (path: string, folderMap: Map<string, any>) => {
    const node = folderMap.get(path);
    if (!node) return null;

    const isExpanded = expandedFolders.has(node.path);
    const isSelected = selectedFolder === node.path;
    const hasChildren = node.children.length > 0;

    return (
      <div key={node.path}>
        <div
          className={`flex items-center gap-2 px-3 py-2 rounded-md cursor-pointer transition-colors group ${
            isSelected ? 'bg-blue-50 text-blue-700' : 'hover:bg-gray-100'
          }`}
          style={{ paddingLeft: `${12 + node.level * 16}px` }}
          onClick={() => onFolderSelect(node.path)}
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
        </div>
        {isExpanded && hasChildren && node.level < 2 && (
          <div>
            {node.children.map((childPath: string) => renderFolderNode(childPath, folderMap))}
          </div>
        )}
      </div>
    );
  };

  const { folderMap, rootFolders } = buildFolderTree();
  const favoriteCount = prompts.filter(p => p.isFavorite).length;

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
            <span className="ml-auto text-xs text-gray-500">{prompts.length}</span>
          </Button>

          <Button
            variant="ghost"
            className="w-full justify-start"
            onClick={() => {
              onViewChange('main');
              onFolderSelect(null);
            }}
          >
            <Star className="w-4 h-4 mr-3" />
            收藏
            <span className="ml-auto text-xs text-gray-500">{favoriteCount}</span>
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
          <div className="px-3 py-2">
            <p className="text-xs text-gray-500 uppercase tracking-wide">文件夹</p>
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
