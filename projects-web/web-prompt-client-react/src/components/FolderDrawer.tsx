import { useState } from 'react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from './ui/sheet';
import { Button } from './ui/button';
import { Folder, ChevronRight, ChevronDown, FolderOpen } from 'lucide-react';
import { Prompt } from '../App';

type FolderNode = {
  name: string;
  path: string;
  level: number;
  children: FolderNode[];
  count: number;
};

type FolderDrawerProps = {
  isOpen: boolean;
  onClose: () => void;
  prompts: Prompt[];
  selectedFolder: string | null;
  onSelectFolder: (folder: string | null) => void;
};

export function FolderDrawer({
  isOpen,
  onClose,
  prompts,
  selectedFolder,
  onSelectFolder,
}: FolderDrawerProps) {
  const [expandedFolders, setExpandedFolders] = useState<Set<string>>(new Set());

  // 构建文件夹树结构
  const buildFolderTree = (): FolderNode[] => {
    const folderMap = new Map<string, FolderNode>();
    const rootFolders: FolderNode[] = [];

    // 统计每个文件夹的 prompt 数量
    const folderCounts = new Map<string, number>();
    prompts.forEach(prompt => {
      if (prompt.folder) {
        const parts = prompt.folder.split('/');
        let currentPath = '';
        parts.forEach((part, index) => {
          if (index < 3) { // 最多3级
            currentPath = currentPath ? `${currentPath}/${part}` : part;
            folderCounts.set(currentPath, (folderCounts.get(currentPath) || 0) + 1);
          }
        });
      }
    });

    // 创建所有文件夹节点
    prompts.forEach(prompt => {
      if (!prompt.folder) return;
      
      const parts = prompt.folder.split('/').slice(0, 3); // 限制最多3级
      let currentPath = '';
      
      parts.forEach((part, level) => {
        const parentPath = currentPath;
        currentPath = currentPath ? `${currentPath}/${part}` : part;
        
        if (!folderMap.has(currentPath)) {
          const node: FolderNode = {
            name: part,
            path: currentPath,
            level: level,
            children: [],
            count: folderCounts.get(currentPath) || 0,
          };
          
          folderMap.set(currentPath, node);
          
          if (level === 0) {
            rootFolders.push(node);
          } else {
            const parent = folderMap.get(parentPath);
            if (parent) {
              parent.children.push(node);
            }
          }
        }
      });
    });

    return rootFolders;
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

  const handleSelectFolder = (path: string | null) => {
    onSelectFolder(path);
    onClose();
  };

  const renderFolderNode = (node: FolderNode) => {
    const isExpanded = expandedFolders.has(node.path);
    const isSelected = selectedFolder === node.path;
    const hasChildren = node.children.length > 0;

    return (
      <div key={node.path}>
        <div
          className={`flex items-center gap-2 px-3 py-2.5 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors ${
            isSelected ? 'bg-blue-50 text-blue-600' : ''
          }`}
          style={{ paddingLeft: `${12 + node.level * 20}px` }}
          onClick={() => handleSelectFolder(node.path)}
        >
          {hasChildren && node.level < 2 && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                toggleFolder(node.path);
              }}
              className="flex-shrink-0"
            >
              {isExpanded ? (
                <ChevronDown className="w-4 h-4 text-gray-400" />
              ) : (
                <ChevronRight className="w-4 h-4 text-gray-400" />
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
          <span className="flex-1">{node.name}</span>
          <span className="text-xs text-gray-400">{node.count}</span>
        </div>
        {isExpanded && hasChildren && node.level < 2 && (
          <div>
            {node.children.map(child => renderFolderNode(child))}
          </div>
        )}
      </div>
    );
  };

  const folderTree = buildFolderTree();
  const totalCount = prompts.length;

  return (
    <Sheet open={isOpen} onOpenChange={onClose}>
      <SheetContent side="left" className="w-80 p-0">
        <SheetHeader className="px-6 py-4 border-b">
          <SheetTitle>文件夹</SheetTitle>
        </SheetHeader>
        
        <div className="p-4 space-y-1">
          {/* 全部 Prompts */}
          <div
            className={`flex items-center gap-2 px-3 py-2.5 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors ${
              selectedFolder === null ? 'bg-blue-50 text-blue-600' : ''
            }`}
            onClick={() => handleSelectFolder(null)}
          >
            <div className="w-4" />
            <Folder className={`w-4 h-4 ${selectedFolder === null ? 'text-blue-600' : 'text-gray-500'}`} />
            <span className="flex-1">全部 Prompts</span>
            <span className="text-xs text-gray-400">{totalCount}</span>
          </div>

          {/* 文件夹树 */}
          {folderTree.length > 0 && (
            <>
              <div className="pt-4 pb-2 px-3">
                <p className="text-xs text-gray-500">我的文件夹</p>
              </div>
              {folderTree.map(node => renderFolderNode(node))}
            </>
          )}

          {folderTree.length === 0 && (
            <div className="text-center py-8 text-gray-400 text-sm">
              <Folder className="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>暂无文件夹</p>
              <p className="text-xs mt-1">创建 Prompt 时可以指定文件夹</p>
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
