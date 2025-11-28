import { Button } from './ui/button';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { Badge } from './ui/badge';
import { Separator } from './ui/separator';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from './ui/dialog';
import { Textarea } from './ui/textarea';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { RadioGroup, RadioGroupItem } from './ui/radio-group';
import { 
  MessageSquare, 
  LogOut, 
  Copy,
  Check,
  Send,
  Bug,
  Lightbulb,
  Heart,
  Edit
} from 'lucide-react';
import { toast } from 'sonner';
import { useState } from 'react';
import { copyToClipboard } from '../utils/clipboard';
import { api } from '../services/api';

type ProfileScreenProps = {
  onBack: () => void;
  userId: string;
  userName: string;
  userAvatar: string;
  loginType: 'google' | 'wechat' | 'guest' | 'registered' | 'oauth';
  onLogout?: () => void;
};

export function ProfileScreen({ 
  onBack, 
  userId, 
  userName, 
  userAvatar,
  loginType,
  onLogout 
}: ProfileScreenProps) {
  const [copied, setCopied] = useState(false);
  const [feedbackOpen, setFeedbackOpen] = useState(false);
  const [feedbackType, setFeedbackType] = useState('suggestion');
  const [feedbackContent, setFeedbackContent] = useState('');
  const [feedbackContact, setFeedbackContact] = useState('');
  const [isEditNameOpen, setIsEditNameOpen] = useState(false);
  const [newUserName, setNewUserName] = useState(userName);

  const handleCopyUserId = async () => {
    const success = await copyToClipboard(userId);
    if (success) {
      setCopied(true);
      toast.success('User ID 已复制');
      setTimeout(() => setCopied(false), 2000);
    } else {
      toast.error('复制失败，请手动复制');
    }
  };

  const handleSubmitFeedback = async () => {
    if (!feedbackContent.trim()) {
      toast.error('请输入反馈内容');
      return;
    }

    try {
      await api.feedback.submit({
        type: feedbackType,
        content: feedbackContent,
        contact: feedbackContact
      });
      toast.success('感谢您的反馈！我们会认真考虑您的建议。');
      
      // 重置表单
      setFeedbackOpen(false);
      setFeedbackType('suggestion');
      setFeedbackContent('');
      setFeedbackContact('');
    } catch (error) {
      toast.error('反馈提交失败，请稍后重试');
    }
  };

  const handleUpdateUserName = async () => {
    if (!newUserName.trim()) {
      toast.error('用户名不能为空');
      return;
    }

    try {
      await api.user.updateProfile({ displayName: newUserName.trim() });
      toast.success('用户名修改成功');
      setIsEditNameOpen(false);
      // 触发页面刷新或更新父组件状态（这里为了简单直接刷新页面，或者依赖父组件重新传递props）
      // 更好的做法是onUpdateProfile callback
      setTimeout(() => window.location.reload(), 500); 
    } catch (error) {
      toast.error('用户名修改失败');
    }
  };

  const getFeedbackTypeIcon = (type: string) => {
    switch (type) {
      case 'bug':
        return <Bug className="w-5 h-5" />;
      case 'suggestion':
        return <Lightbulb className="w-5 h-5" />;
      case 'other':
        return <Heart className="w-5 h-5" />;
      default:
        return <MessageSquare className="w-5 h-5" />;
    }
  };

  const getFeedbackTypeLabel = (type: string) => {
    switch (type) {
      case 'bug':
        return '问题反馈';
      case 'suggestion':
        return '功能建议';
      case 'other':
        return '其他反馈';
      default:
        return '';
    }
  };

  const getLoginTypeLabel = () => {
    switch (loginType) {
      case 'google': return 'Google 账号';
      case 'wechat': return '微信账号';
      case 'guest': return '游客模式';
      case 'registered': return '注册账号';
      case 'oauth': return 'OAuth 账号';
    }
  };

  return (
    <div className="h-full flex flex-col bg-white overflow-hidden">
      {/* Header */}
      <div className="border-b px-6 py-4 flex-shrink-0">
        <h2 className="text-2xl">设置</h2>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-y-auto">
        <div className="max-w-2xl mx-auto p-6 space-y-6">
          {/* User Profile Card */}
          <div className="bg-gradient-to-br from-blue-50 to-purple-50 rounded-lg p-8 border border-blue-100">
            <div className="flex items-center gap-6">
              <Avatar className="w-20 h-20 border-4 border-white shadow-md">
                <AvatarImage src={userAvatar} alt={userName} />
                <AvatarFallback className="text-2xl bg-blue-600 text-white">
                  {userName.charAt(0)}
                </AvatarFallback>
              </Avatar>
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <h2 className="text-2xl">{userName}</h2>
                  <Button variant="ghost" size="icon" className="h-6 w-6" onClick={() => setIsEditNameOpen(true)}>
                    <Edit className="w-4 h-4 text-gray-500" />
                  </Button>
                </div>
                <Badge variant="secondary" className="mb-3">
                  {getLoginTypeLabel()}
                </Badge>
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <span>User ID:</span>
                  <code className="bg-white px-2 py-1 rounded text-xs">{userId}</code>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-6 w-6"
                    onClick={handleCopyUserId}
                  >
                    {copied ? (
                      <Check className="w-3.5 h-3.5 text-green-600" />
                    ) : (
                      <Copy className="w-3.5 h-3.5" />
                    )}
                  </Button>
                </div>
              </div>
            </div>
          </div>

          {/* Support Section */}
          <div className="bg-white rounded-lg border border-gray-200">
            <div className="p-4 border-b">
              <h3 className="font-medium">帮助与支持</h3>
            </div>
            <div className="divide-y">
              <button 
                className="w-full flex items-center gap-4 px-4 py-4 hover:bg-gray-50 transition-colors text-left"
                onClick={() => setFeedbackOpen(true)}
              >
                <MessageSquare className="w-5 h-5 text-gray-500" />
                <div className="flex-1">
                  <p className="font-medium">意见反馈</p>
                  <p className="text-sm text-gray-500">告诉我们您的建议</p>
                </div>
              </button>
            </div>
          </div>

          {/* About Section */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="font-medium mb-4">关于</h3>
            <div className="space-y-3 text-sm text-gray-600">
              <div className="flex justify-between">
                <span>应用版本</span>
                <span>1.0.0</span>
              </div>
              <Separator />
              <div className="flex justify-between">
                <span>构建日期</span>
                <span>2025-11-04</span>
              </div>
              <Separator />
              <p className="text-xs text-gray-400 pt-3">
                Prompt Manager - 多平台 Prompt 管理工具
              </p>
            </div>
          </div>

          {/* Logout */}
          <div className="pb-6">
            <Button 
              variant="outline" 
              className="w-full text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200"
              onClick={() => {
                toast.success('已退出登录');
                setTimeout(() => window.location.reload(), 1000);
                if (onLogout) {
                  onLogout();
                }
              }}
            >
              <LogOut className="w-4 h-4 mr-2" />
              退出登录
            </Button>
          </div>
        </div>
      </div>

      {/* Edit UserName Dialog */}
      <Dialog open={isEditNameOpen} onOpenChange={setIsEditNameOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>修改用户名</DialogTitle>
          </DialogHeader>
          <div className="py-4">
            <Label htmlFor="username">用户名</Label>
            <Input
              id="username"
              value={newUserName}
              onChange={(e) => setNewUserName(e.target.value)}
              className="mt-2"
              autoFocus
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditNameOpen(false)}>取消</Button>
            <Button onClick={handleUpdateUserName}>保存</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Feedback Dialog */}
      <Dialog open={feedbackOpen} onOpenChange={setFeedbackOpen}>
        <DialogContent className="sm:max-w-xl max-h-[90vh] flex flex-col p-0 overflow-hidden">
          {/* Header - Fixed */}
          <div className="flex-shrink-0 p-6 pb-0">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2 text-xl">
                <MessageSquare className="w-6 h-6 text-blue-600" />
                意见反馈
              </DialogTitle>
              <DialogDescription>
                您的反馈对我们非常重要，帮助我们做得更好
              </DialogDescription>
            </DialogHeader>
          </div>

          {/* Content - Scrollable */}
          <div className="flex-1 overflow-y-auto px-6">
            <div className="space-y-6 py-4">
              {/* Feedback Type */}
              <div>
                <Label className="text-base mb-3 block">反馈类型 *</Label>
                <RadioGroup value={feedbackType} onValueChange={setFeedbackType}>
                  <div className="grid grid-cols-3 gap-3">
                    <label
                      htmlFor="type-bug"
                      className={`flex flex-col items-center gap-2 p-4 rounded-lg border-2 cursor-pointer transition-all ${
                        feedbackType === 'bug'
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <RadioGroupItem value="bug" id="type-bug" className="sr-only" />
                      <Bug className={`w-6 h-6 ${feedbackType === 'bug' ? 'text-blue-600' : 'text-gray-500'}`} />
                      <span className={`text-sm ${feedbackType === 'bug' ? 'font-medium text-blue-700' : 'text-gray-700'}`}>
                        问题反馈
                      </span>
                    </label>

                    <label
                      htmlFor="type-suggestion"
                      className={`flex flex-col items-center gap-2 p-4 rounded-lg border-2 cursor-pointer transition-all ${
                        feedbackType === 'suggestion'
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <RadioGroupItem value="suggestion" id="type-suggestion" className="sr-only" />
                      <Lightbulb className={`w-6 h-6 ${feedbackType === 'suggestion' ? 'text-blue-600' : 'text-gray-500'}`} />
                      <span className={`text-sm ${feedbackType === 'suggestion' ? 'font-medium text-blue-700' : 'text-gray-700'}`}>
                        功能建议
                      </span>
                    </label>

                    <label
                      htmlFor="type-other"
                      className={`flex flex-col items-center gap-2 p-4 rounded-lg border-2 cursor-pointer transition-all ${
                        feedbackType === 'other'
                          ? 'border-blue-500 bg-blue-50'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                    >
                      <RadioGroupItem value="other" id="type-other" className="sr-only" />
                      <Heart className={`w-6 h-6 ${feedbackType === 'other' ? 'text-blue-600' : 'text-gray-500'}`} />
                      <span className={`text-sm ${feedbackType === 'other' ? 'font-medium text-blue-700' : 'text-gray-700'}`}>
                        其他反馈
                      </span>
                    </label>
                  </div>
                </RadioGroup>
              </div>

              {/* Feedback Content */}
              <div>
                <Label htmlFor="feedback-content" className="text-base mb-3 block">
                  反馈内容 *
                </Label>
                <Textarea
                  id="feedback-content"
                  placeholder={
                    feedbackType === 'bug'
                      ? '请详细描述您遇到的问题，包括操作步骤和预期结果...'
                      : feedbackType === 'suggestion'
                      ? '请告诉我们您希望增加的功能或改进的地方...'
                      : '请输入您的反馈内容...'
                  }
                  value={feedbackContent}
                  onChange={(e) => setFeedbackContent(e.target.value)}
                  className="min-h-[150px] resize-none"
                  maxLength={500}
                />
                <p className="text-xs text-gray-500 mt-2">
                  {feedbackContent.length} / 500 字符
                </p>
              </div>

              {/* Contact Information */}
              <div>
                <Label htmlFor="feedback-contact" className="text-base mb-3 block">
                  联系方式（可选）
                </Label>
                <Input
                  id="feedback-contact"
                  type="text"
                  placeholder="邮箱或其他联系方式，以便我们跟进"
                  value={feedbackContact}
                  onChange={(e) => setFeedbackContact(e.target.value)}
                />
                <p className="text-xs text-gray-500 mt-2">
                  如果需要我们回复，请留下您的联系方式
                </p>
              </div>

              {/* Tips */}
              <div className="bg-blue-50 rounded-lg p-4 border border-blue-100">
                <div className="flex gap-3">
                  <div className="flex-shrink-0 mt-0.5">
                    {getFeedbackTypeIcon(feedbackType)}
                  </div>
                  <div className="flex-1">
                    <p className="text-sm text-blue-900 mb-1">
                      <strong>{getFeedbackTypeLabel(feedbackType)}提示：</strong>
                    </p>
                    <p className="text-xs text-blue-700">
                      {feedbackType === 'bug'
                        ? '请尽可能详细地描述问题，包括重现步骤、截图等信息，这将帮助我们更快地定位和解决问题。'
                        : feedbackType === 'suggestion'
                        ? '您的建议是我们前进的动力！请告诉我们您期望的功能和使用场景，我们会认真评估并优先考虑。'
                        : '无论是表扬、吐槽还是其他任何想法，我们都非常乐意倾听。您的每一条反馈都是对我们最大的支持！'}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Footer - Fixed */}
          <div className="flex-shrink-0 p-6 pt-0">
            <DialogFooter className="gap-2">
              <Button
                variant="outline"
                onClick={() => setFeedbackOpen(false)}
              >
                取消
              </Button>
              <Button
                onClick={handleSubmitFeedback}
                disabled={!feedbackContent.trim()}
                className="gap-2"
              >
                <Send className="w-4 h-4" />
                提交反馈
              </Button>
            </DialogFooter>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
