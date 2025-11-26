import React, { useState } from 'react';
import { Prompt } from '../App';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Input } from './ui/input';
import { Star, Clock, Copy, Search, Share2, Trash2, Edit } from 'lucide-react';
import { toast } from 'sonner';
import { copyToClipboard } from '../utils/clipboard';
import { ContextMenu, ContextMenuContent, ContextMenuItem, ContextMenuTrigger, ContextMenuSeparator } from './ui/context-menu';

type MainScreenProps = {
  prompts: Prompt[];
  selectedFolder: string | null;
  selectedFolderName: string;
  filterType: 'all' | 'favorites' | 'recent';
  onPromptClick: (prompt: Prompt) => void;
  onToggleFavorite: (id: string) => void;
  onDelete?: (id: string) => void;
  onEdit?: (prompt: Prompt) => void;
  isLoading?: boolean;
};

export function MainScreen({ 
  prompts, 
  selectedFolder,
  selectedFolderName,
  filterType,
  onPromptClick, 
  onToggleFavorite,
  onDelete,
  onEdit,
  isLoading = false
}: MainScreenProps) {
  const [searchQuery, setSearchQuery] = useState('');

  // 简化筛选逻辑，只处理搜索
  const filteredPrompts = prompts.filter(p => {
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      return (
        p.title.toLowerCase().includes(query) ||
        p.content.toLowerCase().includes(query) ||
        p.tags.some(tag => tag.toLowerCase().includes(query))
      );
    }
    return true;
  });

  const handleQuickCopy = async (e: React.MouseEvent | undefined, content: string) => {
    e?.stopPropagation();
    const success = await copyToClipboard(content);
    if (success) {
      toast.success('已复制到剪贴板');
    } else {
      toast.error('复制失败，请手动复制');
    }
  };

  const handleToggleFavorite = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    onToggleFavorite(id);
  };

  const handleShare = (e: React.MouseEvent | undefined, prompt: Prompt) => {
    e?.stopPropagation();
    // 简单的分享实现：复制包含标题和内容的文本
    const shareText = `${prompt.title}\n\n${prompt.content}`;
    copyToClipboard(shareText);
    toast.success('Prompt 内容已复制，可直接粘贴分享');
  };

  const handleDelete = (e: React.MouseEvent | undefined, id: string, title: string) => {
    e?.stopPropagation();
    const confirmed = confirm(`确定要删除 "${title}" 吗？`);
    if (confirmed && onDelete) {
      onDelete(id);
    }
  };

  const handleEdit = (e: React.MouseEvent | undefined, prompt: Prompt) => {
    e?.stopPropagation();
    if (onEdit) {
      onEdit(prompt);
    }
  };

  return (
    <div className="h-full flex flex-col bg-white overflow-hidden">
      {/* Header */}
      <div className="border-b px-6 py-4 flex-shrink-0">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-2xl">
              {selectedFolderName || '全部 Prompts'}
            </h2>
            {selectedFolder && (
              <p className="text-sm text-gray-500 mt-1">文件夹: {selectedFolderName}</p>
            )}
          </div>
          <div className="flex items-center gap-3">
            <div className="relative w-64">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <Input
                placeholder="搜索 Prompt..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Prompt List */}
      <div className="flex-1 overflow-y-auto">
        {isLoading ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <div className="mb-4 inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent"></div>
              <p className="text-gray-600">加载中...</p>
            </div>
          </div>
        ) : filteredPrompts.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <p className="text-gray-400 text-lg mb-2">
                {searchQuery ? '未找到匹配的 Prompt' : '暂无 Prompt'}
              </p>
              <p className="text-gray-400 text-sm">
                {searchQuery ? '试试其他关键词' : '点击左侧按钮创建第一个 Prompt'}
              </p>
            </div>
          </div>
        ) : (
          <div className="p-6">
            <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
              {filteredPrompts.map((prompt) => (
                <ContextMenu key={prompt.id}>
                  <ContextMenuTrigger>
                    <div
                      onClick={() => onPromptClick(prompt)}
                      className="bg-white rounded-lg p-5 border border-gray-200 hover:border-blue-300 hover:shadow-md transition-all cursor-pointer group"
                    >
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1">
                            <h3 className="font-medium text-gray-900 truncate group-hover:text-blue-600 transition-colors">
                              {prompt.title}
                            </h3>
                            <button
                              onClick={(e) => handleToggleFavorite(e, prompt.id)}
                              className="flex-shrink-0"
                            >
                              <Star 
                                className={`w-4 h-4 ${
                                  prompt.isFavorite 
                                    ? 'text-yellow-500 fill-yellow-500' 
                                    : 'text-gray-300 hover:text-yellow-500'
                                } transition-colors`}
                              />
                            </button>
                          </div>
                          <p className="text-sm text-gray-500 line-clamp-2 mb-3">
                            {prompt.content}
                          </p>
                        </div>
                      </div>

                      <div className="flex items-center justify-between">
                        <div className="flex gap-1.5 flex-wrap">
                          <Badge variant="outline" className="text-xs">
                            {prompt.category}
                          </Badge>
                          {prompt.tags.slice(0, 2).map((tag) => (
                            <Badge key={tag} variant="secondary" className="text-xs">
                              {tag}
                            </Badge>
                          ))}
                          {prompt.tags.length > 2 && (
                            <Badge variant="secondary" className="text-xs">
                              +{prompt.tags.length - 2}
                            </Badge>
                          )}
                        </div>

                        <div className="flex items-center gap-2">
                          <div className="flex items-center gap-1 text-gray-400 text-xs">
                            <Clock className="w-3.5 h-3.5" />
                            <span>{prompt.usageCount}</span>
                          </div>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-7 w-7 opacity-0 group-hover:opacity-100 transition-opacity"
                            onClick={(e) => handleQuickCopy(e, prompt.content)}
                          >
                            <Copy className="w-3.5 h-3.5" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  </ContextMenuTrigger>
                  <ContextMenuContent className="w-48">
                    <ContextMenuItem onClick={(e) => handleShare(e, prompt)}>
                      <Share2 className="w-4 h-4 mr-2" />
                      分享
                    </ContextMenuItem>
                    <ContextMenuItem onClick={(e) => handleEdit(e, prompt)}>
                      <Edit className="w-4 h-4 mr-2" />
                      编辑
                    </ContextMenuItem>
                    <ContextMenuItem onClick={(e) => handleQuickCopy(e, prompt.content)}>
                      <Copy className="w-4 h-4 mr-2" />
                      复制内容
                    </ContextMenuItem>
                    <ContextMenuSeparator />
                    <ContextMenuItem 
                      onClick={(e) => handleDelete(e, prompt.id, prompt.title)}
                      className="text-red-600 focus:text-red-600 focus:bg-red-50"
                    >
                      <Trash2 className="w-4 h-4 mr-2" />
                      删除
                    </ContextMenuItem>
                  </ContextMenuContent>
                </ContextMenu>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
