import { useState } from 'react';
import { Prompt } from '../App';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Badge } from './ui/badge';
import { Label } from './ui/label';
import { 
  ArrowLeft, 
  Copy, 
  Star, 
  Edit, 
  Trash2, 
  Share2, 
  Sparkles,
  Clock,
  Calendar
} from 'lucide-react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from './ui/alert-dialog';
import { toast } from 'sonner';
import { copyToClipboard } from '../utils/clipboard';
import type { Folder as ApiFolder } from '../types/api';

type PromptDetailScreenProps = {
  prompt: Prompt;
  onBack: () => void;
  onEdit: () => void;
  onDelete: (id: string) => void;
  onToggleFavorite: (id: string) => void;
  onUse: (id: string) => void;
  folders: ApiFolder[];
};

export function PromptDetailScreen({ 
  prompt, 
  onBack, 
  onEdit, 
  onDelete, 
  onToggleFavorite,
  onUse,
  folders
}: PromptDetailScreenProps) {
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showVariables, setShowVariables] = useState(false);
  const [variables, setVariables] = useState<Record<string, string>>({});
  const [processedContent, setProcessedContent] = useState(prompt.content);

  // Extract variables from content
  const extractVariables = () => {
    const matches = prompt.content.match(/\{(\w+)\}/g);
    if (matches) {
      const vars = matches.map(m => m.slice(1, -1));
      return Array.from(new Set(vars));
    }
    return [];
  };

  const variableList = extractVariables();

  const handleReplaceVariables = () => {
    let content = prompt.content;
    Object.entries(variables).forEach(([key, value]) => {
      content = content.replace(new RegExp(`\\{${key}\\}`, 'g'), value as string);
    });
    setProcessedContent(content);
    setShowVariables(false);
  };

  const handleCopy = async (content: string) => {
    const success = await copyToClipboard(content);
    if (success) {
      toast.success('已复制到剪贴板');
      onUse(prompt.id);
    } else {
      toast.error('复制失败，请手动复制');
    }
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: prompt.title,
          text: prompt.content
        });
      } catch (err) {
        // 用户取消分享，不显示错误
      }
    } else {
      const success = await copyToClipboard(`${prompt.title}\n\n${prompt.content}`);
      if (success) {
        toast.success('分享内容已复制到剪贴板');
      } else {
        toast.error('复制失败，请手动复制');
      }
    }
  };

  const handleDelete = () => {
    onDelete(prompt.id);
    toast.success('已删除');
  };

  // 根据文件夹ID获取文件夹完整路径
  const getFolderPath = (folderId: string | undefined): string => {
    if (!folderId) return '';
    const path: string[] = [];
    let currentId: string | undefined = folderId;
    
    let depth = 0;
    while (currentId && depth < 10) {
      const folderItem = folders.find(f => f.id === currentId);
      if (folderItem) {
        path.unshift(folderItem.name);
        currentId = folderItem.parentId || undefined;
      } else {
        // 如果找不到（可能是ID），如果path为空则显示ID
        if (path.length === 0) path.push(currentId);
        break;
      }
      depth++;
    }
    return path.join(' / ');
  };

  return (
    <div className="h-full flex flex-col bg-white overflow-hidden">
      {/* Header */}
      <div className="border-b px-6 py-4 flex-shrink-0">
        <div className="flex items-center justify-between">
          <Button variant="ghost" size="icon" onClick={onBack}>
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex items-center gap-2">
            <Button variant="outline" onClick={handleShare}>
              <Share2 className="w-4 h-4 mr-2" />
              分享
            </Button>
            <Button variant="outline" onClick={onEdit}>
              <Edit className="w-4 h-4 mr-2" />
              编辑
            </Button>
            <Button 
              variant="outline" 
              onClick={() => setShowDeleteDialog(true)}
              className="text-red-600 hover:text-red-700"
            >
              <Trash2 className="w-4 h-4 mr-2" />
              删除
            </Button>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-4xl mx-auto p-6 space-y-6">
          {/* Title Card */}
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="flex items-start justify-between mb-4">
              <h1 className="text-3xl">{prompt.title}</h1>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => onToggleFavorite(prompt.id)}
              >
                <Star 
                  className={`w-6 h-6 ${prompt.isFavorite ? 'text-yellow-500 fill-yellow-500' : 'text-gray-300'}`} 
                />
              </Button>
            </div>
            
            <div className="flex gap-2 flex-wrap">
              <Badge variant="outline" className="text-sm">{prompt.category}</Badge>
              {prompt.folder && (
                <Badge variant="secondary" className="text-sm">
                  {getFolderPath(prompt.folder)}
                </Badge>
              )}
              {prompt.tags.map((tag) => (
                <Badge key={tag} variant="secondary" className="text-sm">
                  {tag}
                </Badge>
              ))}
            </div>

            {/* Stats */}
            <div className="grid grid-cols-3 gap-4 mt-6 pt-6 border-t">
              <div>
                <p className="text-sm text-gray-500 mb-1 flex items-center gap-1.5">
                  <Clock className="w-4 h-4" />
                  使用次数
                </p>
                <p className="text-xl">{prompt.usageCount}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500 mb-1 flex items-center gap-1.5">
                  <Calendar className="w-4 h-4" />
                  创建时间
                </p>
                <p className="text-sm">{prompt.createdAt.toLocaleDateString()}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500 mb-1 flex items-center gap-1.5">
                  <Calendar className="w-4 h-4" />
                  更新时间
                </p>
                <p className="text-sm">{prompt.updatedAt.toLocaleDateString()}</p>
              </div>
            </div>
          </div>

          {/* Content Card */}
          <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
            <div className="flex items-center justify-between mb-4">
              <Label className="text-base">Prompt 内容</Label>
              <div className="flex gap-2">
                {variableList.length > 0 && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setShowVariables(!showVariables)}
                  >
                    <Sparkles className="w-4 h-4 mr-2" />
                    {showVariables ? '隐藏变量' : '填充变量'}
                  </Button>
                )}
                <Button
                  size="sm"
                  onClick={() => handleCopy(processedContent)}
                >
                  <Copy className="w-4 h-4 mr-2" />
                  复制使用
                </Button>
              </div>
            </div>
            
            <div className="bg-gray-50 rounded-lg p-4 whitespace-pre-wrap text-sm border">
              {processedContent}
            </div>
          </div>

          {/* Variables Panel */}
          {showVariables && variableList.length > 0 && (
            <div className="bg-gradient-to-br from-purple-50 to-blue-50 rounded-lg p-6 shadow-sm border border-purple-100">
              <Label className="text-base mb-4 block flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-purple-600" />
                填充变量值
              </Label>
              <div className="space-y-4">
                {variableList.map((variable) => (
                  <div key={variable}>
                    <Label className="text-sm mb-2 block">
                      {variable}
                    </Label>
                    <Input
                      placeholder={`输入 ${variable} 的值`}
                      value={variables[variable] || ''}
                      onChange={(e) => setVariables({ ...variables, [variable]: e.target.value })}
                    />
                  </div>
                ))}
                <Button 
                  className="w-full" 
                  onClick={handleReplaceVariables}
                  disabled={Object.keys(variables).length === 0}
                >
                  应用变量并预览
                </Button>
              </div>
            </div>
          )}

          {prompt.folder && (
            <div className="text-sm text-gray-500 text-center py-4">
              完整路径：{getFolderPath(prompt.folder)}
            </div>
          )}
        </div>
      </div>

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              确定要删除「{prompt.title}」吗？此操作无法恢复。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} className="bg-red-600 hover:bg-red-700">
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
