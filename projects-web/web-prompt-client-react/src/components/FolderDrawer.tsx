import { useState, useEffect } from 'react';
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from './ui/sheet';
import { Button } from './ui/button';
import { Folder, ChevronRight, ChevronDown, FolderOpen, Plus } from 'lucide-react';
import { Prompt } from '../App';
import { api } from '../services/api';
import type { Folder as ApiFolder } from '../types/api';

type FolderNode = {
  id: string;
  name: string;
  parentId?: string;
  level: number;
  children: FolderNode[];
  promptCount: number;
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
  const [folders, setFolders] = useState<ApiFolder[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');

  // 加载文件夹列表
  useEffect(() => {
    if (isOpen) {
      loadFolders();
    }
  }, [isOpen]);

  const loadFolders = async () => {
    try {
      setIsLoading(true);
      const folderList = await api.folder.getFolders();
      setFolders(folderList);
    } catch (error) {
      console.error('加载文件夹失败:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 构建文件夹树结构
  const buildFolderTree = (): FolderNode[] => {
    const folderMap = new Map<string, FolderNode>();
    const rootFolders: FolderNode[] = [];

    // 创建所有文件夹节点
    folders.forEach(folder => {
      const node: FolderNode = {
        id: folder.id,
        name: folder.name,
        parentId: folder.parentId,
        level: 0,
        children: [],
        promptCount: folder.promptCount,
      };
      folderMap.set(folder.id, node);
    });

    // 构建层级关系
    folders.forEach(folder => {
      const node = folderMap.get(folder.id);
      if (node && folder.parentId) {
        const parent = folderMap.get(folder.parentId);
        if (parent) {
          node.level = parent.level + 1;
          parent.children.push(node);
        }
      } else if (node) {
        rootFolders.push(node);
      }
    });

    // 排序
    const sortFolders = (nodes: FolderNode[]): FolderNode[] => {
      return nodes.sort((a, b) => a.name.localeCompare(b.name));
    };

    return sortFolders(rootFolders);
  };

  const toggleFolder = (id: string) => {
    const newExpanded = new Set(expandedFolders);
    if (newExpanded.has(id)) {
      newExpanded.delete(id);
    } else {
      newExpanded.add(id);
    }
    setExpandedFolders(newExpanded);
  };

  const handleSelectFolder = (id: string | null) => {
    onSelectFolder(id);
    onClose();
  };

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) {
      alert('请输入文件夹名称');
      return;
    }

    try {
      await api.folder.createFolder({ name: newFolderName.trim() });
      setNewFolderName('');
      setShowCreateForm(false);
      await loadFolders(); // 重新加载文件夹列表
    } catch (error) {
      console.error('创建文件夹失败:', error);
      alert('创建文件夹失败，请重试');
    }
  };

  const handleCancelCreate = () => {
    setNewFolderName('');
    setShowCreateForm(false);
  };

  const renderFolderNode = (node: FolderNode) => {
    const isExpanded = expandedFolders.has(node.id);
    const isSelected = selectedFolder === node.id;
    const hasChildren = node.children.length > 0;

    return (
      <div key={node.id}>
        <div
          className={`flex items-center gap-2 px-3 py-2.5 rounded-lg hover:bg-gray-100 cursor-pointer transition-colors ${
            isSelected ? 'bg-blue-50 text-blue-600' : ''
          }`}
          style={{ paddingLeft: `${12 + node.level * 20}px` }}
          onClick={() => handleSelectFolder(node.id)}
        >
          {hasChildren && node.level < 2 && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                toggleFolder(node.id);
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
          <span className="text-xs text-gray-400">{node.promptCount}</span>
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
          {/* 创建文件夹按钮 */}
          <div className="pb-2">
            <Button
              onClick={() => setShowCreateForm(true)}
              className="w-full flex items-center gap-2"
              variant="outline"
            >
              <Plus className="w-4 h-4" />
              新建文件夹
            </Button>
          </div>

          {/* 创建文件夹表单 */}
          {showCreateForm && (
            <div className="p-3 bg-gray-50 rounded-lg space-y-3">
              <input
                type="text"
                value={newFolderName}
                onChange={(e) => setNewFolderName(e.target.value)}
                placeholder="输入文件夹名称"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                autoFocus
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleCreateFolder();
                  } else if (e.key === 'Escape') {
                    handleCancelCreate();
                  }
                }}
              />
              <div className="flex gap-2">
                <Button
                  onClick={handleCreateFolder}
                  className="flex-1"
                  disabled={!newFolderName.trim()}
                >
                  创建
                </Button>
                <Button
                  onClick={handleCancelCreate}
                  variant="outline"
                  className="flex-1"
                >
                  取消
                </Button>
              </div>
            </div>
          )}

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

          {folderTree.length === 0 && !showCreateForm && (
            <div className="text-center py-8 text-gray-400 text-sm">
              <Folder className="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>暂无文件夹</p>
              <p className="text-xs mt-1">点击上方按钮创建文件夹</p>
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
}
