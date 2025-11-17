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

type CreatePromptScreenProps = {
  prompt?: Prompt;
  onSave: (prompt: any) => void;
  onCancel: () => void;
  existingPrompts?: Prompt[];
};

const categories = ['通用', '写作', '编程', '分析', '创意', '营销'];

export function CreatePromptScreen({ prompt, onSave, onCancel, existingPrompts = [] }: CreatePromptScreenProps) {
  const [title, setTitle] = useState(prompt?.title || '');
  const [content, setContent] = useState(prompt?.content || '');
  const [category, setCategory] = useState(prompt?.category || '通用');
  const [folder, setFolder] = useState(prompt?.folder || '');
  const [tags, setTags] = useState<string[]>(prompt?.tags || []);
  const [tagInput, setTagInput] = useState('');
  const [isFolderDialogOpen, setIsFolderDialogOpen] = useState(false);
  const [isFolderSelectOpen, setIsFolderSelectOpen] = useState(false);
  const [newFolderPath, setNewFolderPath] = useState('');
  const [detectedVariables, setDetectedVariables] = useState<string[]>([]);

  // 从现有prompts中提取所有文件夹
  const folders = Array.from(new Set(existingPrompts.map(p => p.folder).filter(Boolean))) as string[];

  // 检测内容中的变量
  useEffect(() => {
    const regex = /\{([^}]+)\}/g;
    const matches = content.matchAll(regex);
    const vars = Array.from(matches, m => m[1]);
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

  const handleCreateFolder = () => {
    if (newFolderPath.trim()) {
      setFolder(newFolderPath.trim());
      setNewFolderPath('');
      setIsFolderDialogOpen(false);
    }
  };

  const handleSelectFolder = (folderPath: string) => {
    setFolder(folderPath);
    setIsFolderSelectOpen(false);
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
        <div className="flex gap-3">
          <Button variant="outline" onClick={onCancel}>
            取消
          </Button>
          <Button 
            onClick={handleSave} 
            disabled={!title.trim() || !content.trim()}
          >
            保存
          </Button>
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
              <Select value={category} onValueChange={setCategory}>
                <SelectTrigger className="mt-3">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {categories.map((cat) => (
                    <SelectItem key={cat} value={cat}>
                      {cat}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Folder */}
            <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
              <Label className="text-base">文件夹</Label>
              <Dialog open={isFolderSelectOpen} onOpenChange={setIsFolderSelectOpen}>
                <Button
                  variant="outline"
                  className="w-full justify-between mt-3"
                  onClick={() => setIsFolderSelectOpen(true)}
                >
                  <span className="flex items-center gap-2 truncate">
                    <Folder className="w-4 h-4 flex-shrink-0 text-gray-500" />
                    <span className="truncate">
                      {folder ? folder.split('/').pop() : '选择文件夹'}
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
                        onClick={() => handleSelectFolder('')}
                        className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg hover:bg-gray-100 transition-colors text-left ${
                          !folder ? 'bg-blue-50 text-blue-700' : ''
                        }`}
                      >
                        <Folder className="w-4 h-4 flex-shrink-0" />
                        <span className="flex-1">无</span>
                        {!folder && <Check className="w-4 h-4 text-blue-600" />}
                      </button>

                      {/* 文件夹列表 */}
                      {folders.sort().map((f) => {
                        const level = f.split('/').length - 1;
                        const isSelected = folder === f;
                        return (
                          <button
                            key={f}
                            onClick={() => handleSelectFolder(f)}
                            className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg hover:bg-gray-100 transition-colors text-left ${
                              isSelected ? 'bg-blue-50 text-blue-700' : ''
                            }`}
                            style={{ paddingLeft: `${16 + level * 20}px` }}
                          >
                            {level > 0 ? (
                              <FolderOpen className="w-4 h-4 flex-shrink-0" />
                            ) : (
                              <Folder className="w-4 h-4 flex-shrink-0" />
                            )}
                            <span className="flex-1 truncate text-sm">{f.split('/').pop()}</span>
                            {isSelected && <Check className="w-4 h-4 text-blue-600 flex-shrink-0" />}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                  
                  <div className="border-t pt-3 mt-3">
                    <Button
                      variant="outline"
                      className="w-full justify-center text-blue-600 hover:text-blue-700"
                      onClick={() => {
                        setIsFolderDialogOpen(true);
                        setIsFolderSelectOpen(false);
                      }}
                    >
                      <Plus className="w-4 h-4 mr-2" />
                      新建文件夹
                    </Button>
                  </div>
                </DialogContent>
              </Dialog>
              
              {folder && (
                <div className="mt-2 text-xs text-gray-500 flex items-center gap-1">
                  <ChevronRight className="w-3 h-3" />
                  <span className="truncate">{folder}</span>
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
              <Label htmlFor="folder-path">
                文件夹路径
              </Label>
              <Input
                id="folder-path"
                placeholder="例如：工作/写作/文案 (最多3级)"
                value={newFolderPath}
                onChange={(e) => setNewFolderPath(e.target.value)}
                className="mt-2"
              />
              <p className="text-xs text-gray-500 mt-2">
                使用 "/" 分隔多级文件夹，最多支持3级目录
              </p>
            </div>
            {newFolderPath && (
              <div className="bg-blue-50 rounded-lg p-3 border border-blue-100">
                <p className="text-xs text-blue-700 mb-1.5">预览路径：</p>
                <div className="flex items-center gap-1 text-sm">
                  <Folder className="w-4 h-4 text-blue-600" />
                  <span className="text-blue-900">
                    {newFolderPath.split('/').slice(0, 3).join(' / ')}
                  </span>
                </div>
              </div>
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsFolderDialogOpen(false)}>
              取消
            </Button>
            <Button 
              onClick={handleCreateFolder}
              disabled={!newFolderPath.trim()}
            >
              确定
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
