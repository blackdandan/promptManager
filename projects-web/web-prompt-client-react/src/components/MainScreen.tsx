import React, { useState } from 'react';
import { Prompt } from '../App';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Tabs, TabsList, TabsTrigger } from './ui/tabs';
import { Input } from './ui/input';
import { Star, Clock, Copy, Search } from 'lucide-react';
import { toast } from 'sonner';
import { copyToClipboard } from '../utils/clipboard';

type MainScreenProps = {
  prompts: Prompt[];
  selectedFolder: string | null;
  selectedFolderName: string;
  onPromptClick: (prompt: Prompt) => void;
  onToggleFavorite: (id: string) => void;
  isLoading?: boolean;
};

export function MainScreen({ 
  prompts, 
  selectedFolder,
  selectedFolderName,
  onPromptClick, 
  onToggleFavorite,
  isLoading = false
}: MainScreenProps) {
  const [activeTab, setActiveTab] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');

  const filteredPrompts = prompts
    .filter(p => {
      // 文件夹筛选
      if (selectedFolder && p.folder !== selectedFolder) return false;
      
      // 标签筛选
      if (activeTab === 'favorites' && !p.isFavorite) return false;
      if (activeTab === 'recent') return true;
      
      // 搜索筛选
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

  const sortedPrompts = activeTab === 'recent' 
    ? [...filteredPrompts].sort((a, b) => b.updatedAt.getTime() - a.updatedAt.getTime())
    : filteredPrompts;

  const handleQuickCopy = async (e: React.MouseEvent, content: string) => {
    e.stopPropagation();
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

        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="all">全部</TabsTrigger>
            <TabsTrigger value="recent">最近</TabsTrigger>
            <TabsTrigger value="favorites">收藏</TabsTrigger>
          </TabsList>
        </Tabs>
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
        ) : sortedPrompts.length === 0 ? (
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
              {sortedPrompts.map((prompt) => (
                <div
                  key={prompt.id}
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
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
