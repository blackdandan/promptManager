import { useState, useEffect } from 'react';
import { Prompt } from '../App';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Textarea } from './ui/textarea';
import { Label } from './ui/label';
import { Badge } from './ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from './ui/dialog';
import { X, Plus, Folder, ChevronRight, Sparkles, AlertCircle, Check, FolderOpen } from 'lucide-react';
import { toast } from 'sonner';
import { api } from '../services/api';
import type { Folder as ApiFolder, Category } from '../types/api';

type FolderTreeNode = ApiFolder & { children: FolderTreeNode[] };

type CreatePromptScreenProps = {
  prompt?: Prompt;
  onSave: (prompt: any) => void;
  onCancel: () => void;
  onFolderCreated?: () => void;
  existingPrompts?: Prompt[];
  folders: ApiFolder[];
  categories: Category[];
  onCategoryCreated?: () => void;
};

export function CreatePromptScreen({ prompt, onSave, onCancel, onFolderCreated, existingPrompts = [], folders, categories, onCategoryCreated }: CreatePromptScreenProps) {
  const [title, setTitle] = useState(prompt?.title || '');
  const [content, setContent] = useState(prompt?.content || '');
  const [category, setCategory] = useState(prompt?.category || '通用');
  const [folder, setFolder] = useState(prompt?.folder || '');
  const [tags, setTags] = useState<string[]>(prompt?.tags || []);
  const [tagInput, setTagInput] = useState('');
  const [isFolderDialogOpen, setIsFolderDialogOpen] = useState(false);
  const [isFolderSelectOpen, setIsFolderSelectOpen] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  const [selectedFolderInDialog, setSelectedFolderInDialog] = useState<string | null>(null);
  const [detectedVariables, setDetectedVariables] = useState<string[]>([]);
  const [expandedFolders, setExpandedFolders] = useState<Set<string>>(new Set());
  const [isCategoryDialogOpen, setIsCategoryDialogOpen] = useState(false);
  const [newCategoryName, setNewCategoryName] = useState('');

  // 获取文件夹深度
  const getFolderDepth = (folderId: string | null): number => {
    if (!folderId) return 0;
    let depth = 1;
    let currentId: string | undefined = folderId;
    
    // 防止死循环
    let safety = 0;
    while (currentId && safety < 10) {
      const folderItem = folders.find(f => f.id === currentId);
      if (folderItem && folderItem.parentId) {
        depth++;
        currentId = folderItem.parentId;
      } else {
        break;
      }
      safety++;
    }
    return depth;
  };

  // 根据文件夹ID获取文件夹完整路径
  const getFolderPath = (folderId: string | null): string => {
    if (!folderId) return '';
    const path: string[] = [];
    let currentId: string | undefined = folderId;
    
    // 防止死循环
    let depth = 0;
    while (currentId && depth < 10) {
      const folderItem = folders.find(f => f.id === currentId);
      if (folderItem) {
        path.unshift(folderItem.name);
        currentId = folderItem.parentId || undefined;
      } else {
        if (path.length === 0 && currentId) path.push(currentId);
        break;
      }
      depth++;
    }
    return path.join(' / ');
  };

  // 切换文件夹展开状态
  const toggleFolder = (folderId: string) => {
    const newExpanded = new Set(expandedFolders);
    if (newExpanded.has(folderId)) {
      newExpanded.delete(folderId);
    } else {
      newExpanded.add(folderId);
    }
    setExpandedFolders(newExpanded);
  };

  // 构建文件夹树结构
  const buildFolderTree = (): FolderTreeNode[] => {
    const folderMap = new Map<string, FolderTreeNode>();
    const rootFolders: FolderTreeNode[] = [];

    // 创建所有文件夹节点
    folders.forEach(folderItem => {
      const node: FolderTreeNode = {
        ...folderItem,
        children: []
      };
      folderMap.set(folderItem.id, node);
    });

    // 构建层级关系
    folders.forEach(folderItem => {
      const node = folderMap.get(folderItem.id);
      if (node && folderItem.parentId) {
        const parent = folderMap.get(folderItem.parentId);
        if (parent) {
          parent.children.push(node);
        }
      } else if (node) {
        rootFolders.push(node);
      }
    });

    // 排序
    const sortFolders = (nodes: FolderTreeNode[]): FolderTreeNode[] => {
      return nodes.sort((a, b) => a.name.localeCompare(b.name));
    };

    return sortFolders(rootFolders);
  };

  // 渲染文件夹节点
  const renderFolderNode = (node: FolderTreeNode, level: number = 0) => {
    const isSelected = selectedFolderInDialog === node.id;
    const hasChildren = node.children.length > 0;
    const isExpanded = expandedFolders.has(node.id);

    return (
      <div key={node.id}>
        <div className="flex items-center">
          {hasChildren && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                toggleFolder(node.id);
              }}
              className="flex-shrink-0 p-1 hover:bg-gray-100 rounded transition-colors"
            >
              <ChevronRight 
                className={`w-3 h-3 text-gray-500 transition-transform ${
                  isExpanded ? 'rotate-90' : ''
                }`} 
              />
            </button>
          )}
          {!hasChildren && <div className="w-5" />}
          <button
            onClick={() => handleSelectFolder(node.id)}
            className={`flex-1 flex items-center gap-3 px-4 py-2.5 rounded-lg hover:bg-gray-100 transition-colors text-left ${
              isSelected ? 'bg-blue-50 text-blue-700' : ''
            }`}
            style={{ paddingLeft: `${8 + level * 20}px` }}
          >
            {hasChildren ? (
              <FolderOpen className={`w-4 h-4 flex-shrink-0 ${isSelected ? 'text-blue-600' : 'text-gray-500'}`} />
            ) : (
              <Folder className={`w-4 h-4 flex-shrink-0 ${isSelected ? 'text-blue-600' : 'text-gray-500'}`} />
            )}
            <span className="flex-1 truncate text-sm">{node.name}</span>
            <span className="text-xs text-gray-400">{node.promptCount}</span>
            {isSelected && <Check className="w-4 h-4 text-blue-600 flex-shrink-0" />}
          </button>
        </div>
        {hasChildren && isExpanded && (
          <div>
            {node.children.map(child => renderFolderNode(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  // 检测内容中的变量
  useEffect(() => {
    const regex = /\{([^}]+)\}/g;
    const matches = content.matchAll(regex);
    const vars: string[] = [];
    for (const match of matches) {
      vars.push(match[1]);
    }
    const uniqueVars = Array.from(new Set(vars));
    setDetectedVariables(uniqueVars);
  }, [content]);

  const handleAddTag = () => {
    if (tagInput.trim() && tags.length < 5 && !tags.includes(tagInput.trim())) {
      setTags([...tags, tagInput.trim()]);
      setTagInput('');
    }
  };

  const handleRemoveTag = (tag: string) => {
    setTags(tags.filter(t => t !== tag));
  };

  const handleSave = () => {
    if (!title.trim() || !content.trim()) {
      return;
    }

    if (prompt) {
      onSave({
        ...prompt,
        title,
        content,
        category,
        folder: folder || undefined,
        tags,
        updatedAt: new Date()
      });
    } else {
      onSave({
        title,
        content,
        category,
        folder: folder || undefined,
        tags,
        isFavorite: false
      });
    }
  };

  const handleCreateFolder = async () => {
    if (!newFolderName.trim()) {
      toast.error('请输入文件夹名称');
      return;
    }

    try {
      const newFolder = await api.folder.createFolder({ 
        name: newFolderName.trim(),
        parentId: selectedFolderInDialog || undefined
      });
      
      setNewFolderName('');
      setIsFolderDialogOpen(false);
      
      // 选中新建的文件夹
      setSelectedFolderInDialog(newFolder.id);
      
      // 如果是在某个文件夹下创建，展开该父文件夹
      if (newFolder.parentId) {
        setExpandedFolders(prev => {
          const next = new Set(prev);
          next.add(newFolder.parentId!);
          return next;
        });
      }

      toast.success('文件夹创建成功');

      // 父组件 loadFolders 会通过 onFolderCreated 触发刷新，从而更新 props.folders
      if (onFolderCreated) {
        onFolderCreated();
      }
    } catch (error) {
      console.error('创建文件夹失败:', error);
      toast.error('创建文件夹失败，请重试');
    }
  };

  const handleSelectFolder = (folderId: string | null) => {
    setSelectedFolderInDialog(folderId);
  };

  const handleConfirmSelectFolder = () => {
    setFolder(selectedFolderInDialog || '');
    setIsFolderSelectOpen(false);
  };

  const handleCancelCreate = () => {
    setNewFolderName('');
    setIsFolderDialogOpen(false);
  };

  const handleCreateCategory = async () => {
    if (!newCategoryName.trim()) {
      toast.error('请输入分类名称');
      return;
    }

    try {
      const newCategory = await api.category.createCategory({ name: newCategoryName.trim() });
      setNewCategoryName('');
      setIsCategoryDialogOpen(false);
      setCategory(newCategory.name); // 自动选中新建的分类
      toast.success('分类创建成功');
      
      if (onCategoryCreated) {
        onCategoryCreated();
      }
    } catch (error) {
      toast.error('分类创建失败');
    }
  };

  const insertVariable = (variable: string) => {
    const cursorPos = (document.getElementById('content') as HTMLTextAreaElement)?.selectionStart || content.length;
    const before = content.substring(0, cursorPos);
    const after = content.substring(cursorPos);
    setContent(`${before}{${variable}}${after}`);
  };

  return (
    <div className="h-full flex flex-col bg-gray-50 overflow-hidden">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4 flex items-center justify-between flex-shrink-0">
        <div>
          <h2 className="text-2xl">{prompt ? '编辑 Prompt' : '新建 Prompt'}</h2>
          {detectedVariables.length > 0 && (
            <p className="text-sm text-gray-500 mt-1">
              已检测到 {detectedVariables.length} 个变量
            </p>
          )}
        </div>
      </div>

      {/* Form */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-4xl mx-auto p-6 space-y-6">
          {/* Title */}
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <Label htmlFor="title" className="text-base">标题 *</Label>
            <Input
              id="title"
              placeholder="例如：文章改写助手、代码审查模板"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="mt-3"
              autoFocus
            />
          </div>

          {/* Content */}
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="flex items-center justify-between mb-3">
              <Label htmlFor="content" className="text-base">内容 *</Label>
              {detectedVariables.length > 0 && (
                <div className="flex items-center gap-2 text-blue-600 text-sm">
                  <Sparkles className="w-4 h-4" />
                  <span>{detectedVariables.length} 个变量</span>
                </div>
              )}
            </div>
            <Textarea
              id="content"
              placeholder="输入Prompt内容，使用 {变量名} 定义变量&#10;&#10;例如：请将以下文章改写为{tone}风格，字数控制在{length}字左右..."
              value={content}
              onChange={(e) => setContent(e.target.value)}
              className="mt-3 min-h-[240px]"
            />
            
            {/* 已检测的变量 */}
            {detectedVariables.length > 0 && (
              <div className="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-100">
                <p className="text-sm text-blue-700 mb-2 flex items-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  检测到以下变量：
                </p>
                <div className="flex gap-2 flex-wrap">
                  {detectedVariables.map((v) => (
                    <Badge key={v} variant="secondary" className="bg-white text-blue-700 border border-blue-200">
                      {`{${v}}`}
                    </Badge>
                  ))}
                </div>
              </div>
            )}

            {detectedVariables.length === 0 && (
              <p className="text-sm text-gray-500 mt-3 flex items-center gap-2">
                <Sparkles className="w-4 h-4" />
                使用 {`{变量名}`} 可以创建可替换的变量
              </p>
            )}
          </div>

          {/* Common Variables */}
          <div className="bg-gradient-to-br from-purple-50 to-blue-50 rounded-lg p-5 border border-purple-100">
            <p className="text-sm mb-3 flex items-center gap-2 text-gray-700">
              <Sparkles className="w-5 h-5 text-purple-600" />
              快速插入常用变量：
            </p>
            <div className="flex gap-2 flex-wrap">
              {['topic', 'tone', 'length', 'format', 'language', 'style'].map((variable) => (
                <Badge
                  key={variable}
                  variant="outline"
                  className="cursor-pointer bg-white hover:bg-purple-100 hover:border-purple-300 transition-colors px-3 py-1.5"
                  onClick={() => insertVariable(variable)}
                >
                  {`{${variable}}`}
                </Badge>
              ))}
            </div>
          </div>

          {/* Category & Folder Row */}
          <div className="grid grid-cols-2 gap-4">
            {/* Category */}
            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <Label className="text-base">分类</Label>
              <div className="flex gap-2 mt-3">
                <Select value={category} onValueChange={setCategory}>
                  <SelectTrigger className="flex-1">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((cat) => (
                      <SelectItem key={cat.id} value={cat.name}>
                        {cat.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Button variant="outline" size="icon" onClick={() => setIsCategoryDialogOpen(true)}>
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
            </div>

            {/* Folder */}
            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <Label className="text-base">文件夹</Label>
              <Dialog 
                open={isFolderSelectOpen} 
                onOpenChange={(open) => {
                  setIsFolderSelectOpen(open);
                  if (open) {
                    setSelectedFolderInDialog(folder || null);
                  }
                }}
              >
                <Button
                  variant="outline"
                  className="w-full justify-between mt-3"
                  onClick={() => setIsFolderSelectOpen(true)}
                >
                  <span className="flex items-center gap-2 truncate">
                    <Folder className="w-4 h-4 flex-shrink-0 text-gray-500" />
                    <span className="truncate">
                      {folder ? getFolderPath(folder) : '选择文件夹'}
                    </span>
                  </span>
                  <ChevronRight className="w-4 h-4 flex-shrink-0" />
                </Button>
                
                <DialogContent className="max-w-md">
                  <DialogHeader>
                    <DialogTitle className="flex items-center gap-2">
                      <Folder className="w-5 h-5 text-blue-600" />
                      选择文件夹
                    </DialogTitle>
                  </DialogHeader>
                  
                  <div className="max-h-96 overflow-y-auto">
                    <div className="space-y-1 pr-4">
                      {/* 无文件夹选项 */}
                      <button
                        onClick={() => handleSelectFolder(null)}
                        className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg hover:bg-gray-100 transition-colors text-left ${
                          !selectedFolderInDialog ? 'bg-blue-50 text-blue-700' : ''
                        }`}
                      >
                        <Folder className="w-4 h-4 flex-shrink-0" />
                        <span className="flex-1">无</span>
                        {!selectedFolderInDialog && <Check className="w-4 h-4 text-blue-600" />}
                      </button>

                      {/* 文件夹列表 */}
                      {buildFolderTree().map(node => renderFolderNode(node))}
                    </div>
                  </div>
                  
                  <DialogFooter className="flex-col sm:justify-between gap-2 mt-4 border-t pt-4">
                    <div className="flex justify-between w-full items-center">
                      <Button
                        variant="ghost"
                        className="text-blue-600 hover:text-blue-700 p-0 h-auto font-normal hover:bg-transparent"
                        onClick={() => {
                          if (selectedFolderInDialog) {
                            const depth = getFolderDepth(selectedFolderInDialog);
                            if (depth >= 3) {
                              toast.error('不允许创建超过3级的文件夹');
                              return;
                            }
                          }
                          setIsFolderDialogOpen(true);
                        }}
                      >
                        <Plus className="w-4 h-4 mr-1" />
                        新建文件夹
                      </Button>
                      <div className="flex gap-2">
                        <Button variant="outline" onClick={() => setIsFolderSelectOpen(false)}>
                          取消
                        </Button>
                        <Button onClick={handleConfirmSelectFolder}>
                          确定
                        </Button>
                      </div>
                    </div>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
              
              {folder && (
                <div className="mt-2 text-xs text-gray-500 flex items-center gap-1">
                  <ChevronRight className="w-3 h-3" />
                  <span className="truncate">{getFolderPath(folder)}</span>
                </div>
              )}
            </div>
          </div>

          {/* Tags */}
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <Label className="text-base">
              标签
              <span className="text-sm text-gray-500 ml-2">
                ({tags.length}/5)
              </span>
            </Label>
            
            {tags.length > 0 && (
              <div className="flex gap-2 flex-wrap mt-3">
                {tags.map((tag) => (
                  <Badge key={tag} variant="secondary" className="gap-1.5 pl-3 pr-2 py-1.5">
                    {tag}
                    <button 
                      onClick={() => handleRemoveTag(tag)}
                      className="hover:bg-gray-300 rounded-full p-0.5 transition-colors"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </Badge>
                ))}
              </div>
            )}

            {tags.length < 5 && (
              <div className="flex gap-2 mt-3">
                <Input
                  placeholder={tags.length === 0 ? "添加标签帮助分类" : "继续添加标签"}
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleAddTag();
                    }
                  }}
                />
                <Button
                  variant="outline"
                  size="icon"
                  onClick={handleAddTag}
                  disabled={!tagInput.trim() || tags.length >= 5}
                  className="flex-shrink-0"
                >
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
            )}

            {tags.length === 5 && (
              <p className="text-sm text-amber-600 mt-3 flex items-center gap-2">
                <AlertCircle className="w-4 h-4" />
                已达到标签数量上限
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Footer Actions */}
      <div className="bg-white border-t p-4 flex-shrink-0 z-10">
        <div className="max-w-4xl mx-auto flex gap-3">
          <Button 
            variant="outline" 
            onClick={onCancel} 
            className="flex-1 h-12 text-base hover:bg-gray-100"
          >
            取消
          </Button>
          <Button 
            onClick={handleSave} 
            disabled={!title.trim() || !content.trim()}
            className="flex-1 h-12 text-base font-medium shadow-sm"
          >
            保存 Prompt
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
            <Label htmlFor="category-name">分类名称</Label>
            <Input
              id="category-name"
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

      {/* New Folder Dialog */}
      <Dialog open={isFolderDialogOpen} onOpenChange={setIsFolderDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Folder className="w-5 h-5 text-blue-600" />
              新建文件夹
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="folder-name">
                文件夹名称
              </Label>
              <Input
                id="folder-name"
                placeholder="输入文件夹名称"
                value={newFolderName}
                onChange={(e) => setNewFolderName(e.target.value)}
                className="mt-2"
                autoFocus
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleCreateFolder();
                  } else if (e.key === 'Escape') {
                    handleCancelCreate();
                  }
                }}
              />
              <p className="text-xs text-gray-500 mt-2">
                创建新的文件夹用于组织您的 Prompt
              </p>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleCancelCreate}>
              取消
            </Button>
            <Button 
              onClick={handleCreateFolder}
              disabled={!newFolderName.trim()}
            >
              确定
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
