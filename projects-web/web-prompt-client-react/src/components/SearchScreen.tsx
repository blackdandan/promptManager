import React, { useState } from 'react';
import { Prompt } from '../App';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Badge } from './ui/badge';
import { Search, X, Star, Clock, Copy, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { copyToClipboard } from '../utils/clipboard';
import { Category } from '../types/api';
import { ContextMenu, ContextMenuContent, ContextMenuItem, ContextMenuTrigger } from './ui/context-menu';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from './ui/dialog';
import { Label } from './ui/label';
import { api } from '../services/api';

type SearchScreenProps = {
  prompts: Prompt[];
  categories: Category[];
  onBack: () => void;
  onPromptClick: (prompt: Prompt) => void;
  onCategoryCreated?: () => void;
  onCategoryDeleted?: () => void;
};

export function SearchScreen({ prompts, categories = [], onBack, onPromptClick, onCategoryCreated, onCategoryDeleted }: SearchScreenProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [isCategoryDialogOpen, setIsCategoryDialogOpen] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');

  const handleCreateCategory = async () => {
    if (!newCategoryName.trim()) {
      toast.error('请输入分类名称');
      return;
    }

    try {
      const newCategory = await api.category.createCategory({ name: newCategoryName.trim() });
      setNewCategoryName('');
      setIsCategoryDialogOpen(false);
      toast.success('分类创建成功');
      
      if (onCategoryCreated) {
        onCategoryCreated();
      }
      setSelectedCategory(newCategory.name);
    } catch (error) {
      toast.error('分类创建失败');
    }
  };

  const handleDeleteCategory = async (category: Category) => {
    if (category.isSystem) {
      toast.error('系统分类不允许删除');
      return;
    }

    const confirmed = confirm(`确定要删除分类 "${category.name}" 吗？\n该分类下的 Prompt 将自动转移到"通用"分类。`);
    if (!confirmed) return;

    try {
      await api.category.deleteCategory(category.id);
      toast.success('分类删除成功');
      if (onCategoryDeleted) {
        onCategoryDeleted();
      }
      if (selectedCategory === category.name) {
        setSelectedCategory(null);
      }
    } catch (error) {
      toast.error('分类删除失败');
    }
  };

  const filteredPrompts = prompts.filter(p => {
    const query = searchQuery.toLowerCase();
    const matchesSearch = 
      p.title.toLowerCase().includes(query) ||
      p.content.toLowerCase().includes(query) ||
      p.tags.some(tag => tag.toLowerCase().includes(query));
    
    const matchesCategory = !selectedCategory || p.category === selectedCategory;
    
    return matchesSearch && matchesCategory;
  });

  const handleQuickCopy = async (e: React.MouseEvent, content: string) => {
    e.stopPropagation();
    const success = await copyToClipboard(content);
    if (success) {
      toast.success('已复制到剪贴板');
    } else {
      toast.error('复制失败，请手动复制');
    }
  };

  return (
    <div className="h-full flex flex-col bg-white overflow-hidden">
      {/* Header */}
      <div className="border-b px-6 py-4 flex-shrink-0">
        <div className="flex items-center gap-4 mb-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              placeholder="搜索 Prompt 标题、内容或标签..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10 pr-10 h-12 text-base"
              autoFocus
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery('')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                <X className="w-5 h-5" />
              </button>
            )}
          </div>
        </div>

        {/* Category Filter */}
        <div className="flex gap-2 flex-wrap items-center">
          <Button
            variant={selectedCategory === null ? 'default' : 'outline'}
            size="sm"
            onClick={() => setSelectedCategory(null)}
          >
            全部
          </Button>
          {categories.map((category) => (
            <ContextMenu key={category.id}>
              <ContextMenuTrigger>
                <Button
                  variant={selectedCategory === category.name ? 'default' : 'outline'}
                  size="sm"
                  onClick={() => setSelectedCategory(category.name)}
                  className="cursor-context-menu"
                >
                  {category.name}
                </Button>
              </ContextMenuTrigger>
              <ContextMenuContent>
                <ContextMenuItem 
                  onClick={() => handleDeleteCategory(category)}
                  disabled={category.isSystem}
                  className={category.isSystem ? 'text-gray-400' : 'text-red-600'}
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  删除分类
                </ContextMenuItem>
              </ContextMenuContent>
            </ContextMenu>
          ))}
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsCategoryDialogOpen(true)}
            className="px-2"
          >
            <Plus className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* New Category Dialog */}
      <Dialog open={isCategoryDialogOpen} onOpenChange={setIsCategoryDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>新建分类</DialogTitle>
          </DialogHeader>
          <div className="py-4">
            <Label htmlFor="search-category-name">分类名称</Label>
            <Input
              id="search-category-name"
              value={newCategoryName}
              onChange={(e) => setNewCategoryName(e.target.value)}
              className="mt-2"
              autoFocus
              placeholder="输入分类名称"
              onKeyDown={(e) => {
                 if (e.key === 'Enter') handleCreateCategory();
              }}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCategoryDialogOpen(false)}>取消</Button>
            <Button onClick={handleCreateCategory}>确定</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Results */}
      <div className="flex-1 overflow-y-auto">
        {searchQuery === '' ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <Search className="w-16 h-16 mx-auto mb-4 text-gray-300" />
              <p className="text-gray-400 text-lg mb-2">搜索 Prompt</p>
              <p className="text-gray-400 text-sm">输入关键词开始搜索</p>
            </div>
          </div>
        ) : filteredPrompts.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <p className="text-gray-400 text-lg mb-2">未找到匹配结果</p>
              <p className="text-gray-400 text-sm">试试其他关键词</p>
            </div>
          </div>
        ) : (
          <div className="p-6">
            <p className="text-sm text-gray-500 mb-4">
              找到 {filteredPrompts.length} 个结果
            </p>
            <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
              {filteredPrompts.map((prompt) => (
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
                        {prompt.isFavorite && (
                          <Star className="w-4 h-4 text-yellow-500 fill-yellow-500 flex-shrink-0" />
                        )}
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
