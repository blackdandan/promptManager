import { useState } from 'react';
import { Button } from './ui/button';
import { Separator } from './ui/separator';
import { Zap, UserCircle, Mail } from 'lucide-react';
import { toast } from 'sonner@2.0.3';
import api from '../services/api';
import type { User } from '../types/api';
import { RegisterLoginDialog } from './RegisterLoginDialog';

type LoginScreenProps = {
  onLogin: (user: User) => void;
};

export function LoginScreen({ onLogin }: LoginScreenProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [showRegisterLogin, setShowRegisterLogin] = useState(false);

  // 邮箱密码登录
  const handleEmailLogin = () => {
    setShowRegisterLogin(true);
  };

  // Google登录
  const handleGoogleLogin = async () => {
    setIsLoading(true);
    try {
      // TODO: 实现Google OAuth流程
      // 这里需要前端跳转到Google授权页面，然后在回调中获取授权码
      toast.info('Google登录功能开发中');
    } catch (error) {
      toast.error('Google登录失败: ' + (error instanceof Error ? error.message : '未知错误'));
    } finally {
      setIsLoading(false);
    }
  };

  // 微信登录
  const handleWeChatLogin = async () => {
    setIsLoading(true);
    try {
      // TODO: 实现微信OAuth流程
      toast.info('微信登录功能开发中');
    } catch (error) {
      toast.error('微信登录失败: ' + (error instanceof Error ? error.message : '未知错误'));
    } finally {
      setIsLoading(false);
    }
  };

  // 游客登录
  const handleGuestLogin = async () => {
    setIsLoading(true);
    try {
      // 游客登录：创建匿名用户
      const guestUser: User = {
        userId: `guest_${Date.now()}`,
        username: `游客_${Date.now().toString().slice(-6)}`,
        displayName: '游客用户',
        userType: 'GUEST',
        roles: ['USER'],
      };
      
      // 保存游客信息到localStorage
      localStorage.setItem('user', JSON.stringify(guestUser));
      localStorage.setItem('access_token', 'guest_token_' + Date.now());
      localStorage.setItem('refresh_token', 'guest_refresh_' + Date.now());
      
      onLogin(guestUser);
    } catch (error) {
      toast.error('游客登录失败: ' + (error instanceof Error ? error.message : '未知错误'));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-500 to-purple-600 flex items-center justify-center p-6">
      <div className="w-full max-w-sm">
        {/* Logo and Title */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-white rounded-full mb-4">
            <Zap className="w-8 h-8 text-blue-500" />
          </div>
          <h1 className="text-white mb-2">Prompt Manager</h1>
          <p className="text-blue-100">管理你的AI提示词，随时随地使用</p>
        </div>

        {/* Login Options */}
        <div className="bg-white rounded-2xl p-6 shadow-xl space-y-4">
          {/* Email/Password Login */}
          <Button
            variant="default"
            className="w-full h-12 gap-3"
            onClick={handleEmailLogin}
            disabled={isLoading}
          >
            <Mail className="w-5 h-5" />
            <span>使用邮箱密码登录</span>
          </Button>

          <div className="relative">
            <Separator className="my-4" />
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="bg-white px-2 text-sm text-gray-500">或</span>
            </div>
          </div>

          {/* Google Login */}
          <Button
            variant="outline"
            className="w-full h-12 gap-3"
            onClick={handleGoogleLogin}
            disabled={isLoading}
          >
            <svg className="w-5 h-5" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
              />
            </svg>
            <span>使用Google账号登录</span>
          </Button>

          {/* WeChat Login */}
          <Button
            variant="outline"
            className="w-full h-12 gap-3"
            onClick={handleWeChatLogin}
            disabled={isLoading}
          >
            <svg className="w-5 h-5" viewBox="0 0 24 24" fill="none">
              <path
                d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18z"
                fill="#09B83E"
              />
              <path
                d="M23.744 13.437c0-3.47-3.402-6.297-7.597-6.297-4.194 0-7.596 2.827-7.596 6.297 0 3.47 3.402 6.297 7.596 6.297a9.03 9.03 0 0 0 2.488-.346.845.845 0 0 1 .627.086l1.675.977a.29.29 0 0 0 .147.047.258.258 0 0 0 .254-.26c0-.06-.023-.12-.042-.187l-.344-1.302a.518.518 0 0 1 .187-.584c1.606-1.184 2.605-2.93 2.605-4.735zm-10.245-1.18a1.023 1.023 0 1 1 0-2.046 1.023 1.023 0 0 1 0 2.046zm4.493 0a1.023 1.023 0 1 1 0-2.046 1.023 1.023 0 0 1 0 2.046z"
                fill="#09B83E"
              />
            </svg>
            <span>使用微信账号登录</span>
          </Button>

          <div className="relative">
            <Separator className="my-4" />
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="bg-white px-2 text-sm text-gray-500">或</span>
            </div>
          </div>

          {/* Guest Login */}
          <Button
            variant="secondary"
            className="w-full h-12 gap-3"
            onClick={handleGuestLogin}
            disabled={isLoading}
          >
            {isLoading ? (
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-solid border-current border-r-transparent"></div>
            ) : (
              <UserCircle className="w-5 h-5" />
            )}
            <span>{isLoading ? '登录中...' : '游客模式体验'}</span>
          </Button>

          <p className="text-center text-sm text-gray-500 mt-4">
            游客模式下数据仅保存在本地
          </p>
        </div>

        <p className="text-center text-blue-100 text-sm mt-6">
          支持多端同步 · 安全可靠
        </p>
      </div>

      {/* Register/Login Dialog */}
      <RegisterLoginDialog
        open={showRegisterLogin}
        onOpenChange={setShowRegisterLogin}
        onSuccess={onLogin}
      />
    </div>
  );
}