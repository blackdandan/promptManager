import { useState, useEffect } from "react";
import { LoginScreen } from "./components/LoginScreen";
import { Sidebar } from "./components/Sidebar";
import { MainScreen } from "./components/MainScreen";
import { CreatePromptScreen } from "./components/CreatePromptScreen";
import { PromptDetailScreen } from "./components/PromptDetailScreen";
import { SearchScreen } from "./components/SearchScreen";
import { ProfileScreen } from "./components/ProfileScreen";
import { Toaster, toast } from "sonner";
import api from "./services/api";
import type { Prompt as ApiPrompt, User, Folder as ApiFolder } from "./types/api";

// 本地Prompt类型（为了兼容现有组件）
export type Prompt = {
  id: string;
  title: string;
  content: string;
  tags: string[];
  category: string;
  folder?: string;
  isFavorite: boolean;
  usageCount: number;
  createdAt: Date;
  updatedAt: Date;
};

export type Screen =
  | "login"
  | "main"
  | "create"
  | "edit"
  | "detail"
  | "search"
  | "profile";

// 将API的Prompt转换为本地Prompt格式
function convertApiPrompt(apiPrompt: ApiPrompt): Prompt {
  return {
    id: apiPrompt.id,
    title: apiPrompt.title,
    content: apiPrompt.content,
    tags: apiPrompt.tags,
    category: apiPrompt.category,
    folder: apiPrompt.folderId,
    isFavorite: apiPrompt.isFavorite,
    usageCount: apiPrompt.usageCount,
    createdAt: new Date(apiPrompt.createdAt),
    updatedAt: new Date(apiPrompt.updatedAt),
  };
}

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [currentScreen, setCurrentScreen] = useState<Screen>("login");
  const [currentView, setCurrentView] = useState<"main" | "search" | "profile">("main");
  const [selectedPrompt, setSelectedPrompt] = useState<Prompt | null>(null);
  const [selectedFolder, setSelectedFolder] = useState<string | null>(null);
  const [filterType, setFilterType] = useState<'all' | 'favorites' | 'recent'>('all');
  const [folders, setFolders] = useState<ApiFolder[]>([]);
  const [isLoadingFolders, setIsLoadingFolders] = useState(false);
  const [prompts, setPrompts] = useState<Prompt[]>([]);
  const [isLoadingPrompts, setIsLoadingPrompts] = useState(false);

  // 检查登录状态
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem("access_token");
      const userStr = localStorage.getItem("user");

      if (token && userStr) {
        try {
          const user = JSON.parse(userStr);
          setCurrentUser(user);
          setIsLoggedIn(true);
          setCurrentScreen("main");
          
          // 根据用户类型加载数据
          if (user.userType === 'GUEST') {
            loadLocalPrompts();
          } else {
            await loadPrompts();
          }
        } catch (error) {
          console.error("自动登录失败:", error);
          localStorage.clear();
        }
      }
      setIsLoading(false);
    };

    checkAuth();
  }, []);

  // 加载本地Prompts（游客模式）
  const loadLocalPrompts = () => {
    try {
      const localPromptsStr = localStorage.getItem("guest_prompts");
      if (localPromptsStr) {
        const localPrompts = JSON.parse(localPromptsStr);
        // 将存储的字符串日期转换为Date对象
        const parsedPrompts = localPrompts.map((p: any) => ({
          ...p,
          createdAt: new Date(p.createdAt),
          updatedAt: new Date(p.updatedAt),
        }));
        setPrompts(parsedPrompts);
      } else {
        // 初始化一些示例数据
        const mockPrompts: Prompt[] = [
          {
            id: "1",
            title: "文章改写助手",
            content: "请将以下文章改写为{tone}风格，字数控制在{length}字左右：\n\n{content}",
            tags: ["写作", "改写"],
            category: "写作",
            folder: "工作/写作/文章",
            isFavorite: true,
            usageCount: 0,
            createdAt: new Date(),
            updatedAt: new Date(),
          },
          {
            id: "2",
            title: "代码审查模板",
            content: "请审查以下{language}代码，重点关注：\n1. 代码规范\n2. 性能优化\n3. 安全问题\n\n代码：\n{code}",
            tags: ["编程", "代码审查"],
            category: "编程",
            folder: "开发/后端/审查",
            isFavorite: false,
            usageCount: 0,
            createdAt: new Date(),
            updatedAt: new Date(),
          },
        ];
        setPrompts(mockPrompts);
        saveLocalPrompts(mockPrompts);
      }
    } catch (error) {
      console.error("加载本地数据失败:", error);
      setPrompts([]);
    }
  };

  // 保存本地Prompts（游客模式）
  const saveLocalPrompts = (promptsToSave: Prompt[]) => {
    try {
      localStorage.setItem("guest_prompts", JSON.stringify(promptsToSave));
    } catch (error) {
      console.error("保存本地数据失败:", error);
    }
  };

  // 加载文件夹列表
  const loadFolders = async () => {
    if (currentUser?.userType === 'GUEST') {
      // 游客模式：从prompts中提取文件夹信息
      const folderSet = new Set<string>();
      prompts.forEach(prompt => {
        if (prompt.folder) {
          folderSet.add(prompt.folder);
        }
      });
      const folderList = Array.from(folderSet).map(folderPath => ({
        id: folderPath,
        name: folderPath.split('/').pop() || folderPath,
        parentId: null,
        promptCount: prompts.filter(p => p.folder === folderPath).length,
        createdAt: new Date(),
        updatedAt: new Date()
      }));
      setFolders(folderList);
      return;
    }

    // 真实用户：调用API
    setIsLoadingFolders(true);
    try {
      const folderList = await api.folder.getFolders();
      setFolders(folderList);
    } catch (error) {
      console.error("加载文件夹失败:", error);
    } finally {
      setIsLoadingFolders(false);
    }
  };

  // 加载Prompts（真实用户）
  const loadPrompts = async () => {
    setIsLoadingPrompts(true);
    try {
      const response = await api.prompt.getPrompts({ size: 100 });
      // API返回的是 PageableResponse<Prompt> 格式
      const convertedPrompts = response.content.map(convertApiPrompt);
      setPrompts(convertedPrompts);
      
      // 加载文件夹列表
      await loadFolders();
    } catch (error) {
      toast.error("加载Prompts失败: " + (error instanceof Error ? error.message : "未知错误"));
    } finally {
      setIsLoadingPrompts(false);
    }
  };

  const handleLogin = async (user: User) => {
    setCurrentUser(user);
    setIsLoggedIn(true);
    setCurrentScreen("main");
    setCurrentView("main");
    
    // 只有非游客用户才加载远程数据
    if (user.userType !== 'GUEST') {
      // 加载Prompts
      await loadPrompts();
    } else {
      // 游客模式：加载本地存储的数据
      loadLocalPrompts();
    }
    
    toast.success("登录成功！");
  };

  const handleCreatePrompt = async (
    prompt: Omit<Prompt, "id" | "createdAt" | "updatedAt" | "usageCount">
  ) => {
    // 游客模式：使用本地存储
    if (currentUser?.userType === 'GUEST') {
      const newPrompt: Prompt = {
        ...prompt,
        id: Date.now().toString(),
        createdAt: new Date(),
        updatedAt: new Date(),
        usageCount: 0,
      };
      const updatedPrompts = [newPrompt, ...prompts];
      setPrompts(updatedPrompts);
      saveLocalPrompts(updatedPrompts);
      setCurrentScreen("main");
      toast.success("Prompt 创建成功！");
      return;
    }

    // 真实用户：调用API
    try {
      console.log("开始创建Prompt，请求数据:", {
        title: prompt.title,
        content: prompt.content,
        tags: prompt.tags,
        category: prompt.category,
        isPublic: false,
        folderId: prompt.folder,
      });

      const newPrompt = await api.prompt.createPrompt({
        title: prompt.title,
        content: prompt.content,
        tags: prompt.tags,
        category: prompt.category,
        isPublic: false,
        folderId: prompt.folder,
      });

      console.log("API返回的newPrompt:", newPrompt);
      
      if (!newPrompt) {
        throw new Error("API返回的Prompt数据为空");
      }

      if (!newPrompt.id) {
        throw new Error("API返回的Prompt缺少id字段");
      }

      const convertedPrompt = convertApiPrompt(newPrompt);
      console.log("转换后的Prompt:", convertedPrompt);
      
      setPrompts([convertedPrompt, ...prompts]);
      setCurrentScreen("main");
      toast.success("Prompt 创建成功！");
    } catch (error) {
      console.error("创建Prompt失败:", error);
      toast.error("创建失败: " + (error instanceof Error ? error.message : "未知错误"));
    }
  };

  const handleEditPrompt = async (prompt: Prompt) => {
    // 游客模式：使用本地存储
    if (currentUser?.userType === 'GUEST') {
      const updatedPrompt = {
        ...prompt,
        updatedAt: new Date(),
      };
      const updatedPrompts = prompts.map((p) => (p.id === prompt.id ? updatedPrompt : p));
      setPrompts(updatedPrompts);
      saveLocalPrompts(updatedPrompts);
      setCurrentScreen("detail");
      setSelectedPrompt(updatedPrompt);
      toast.success("Prompt 更新成功！");
      return;
    }

    // 真实用户：调用API
    try {
      const updatedPrompt = await api.prompt.updatePrompt(prompt.id, {
        title: prompt.title,
        content: prompt.content,
        tags: prompt.tags,
        category: prompt.category,
        isFavorite: prompt.isFavorite,
        folderId: prompt.folder,
      });
      
      setPrompts(prompts.map((p) => (p.id === prompt.id ? convertApiPrompt(updatedPrompt) : p)));
      setCurrentScreen("detail");
      setSelectedPrompt(convertApiPrompt(updatedPrompt));
      toast.success("Prompt 更新成功！");
    } catch (error) {
      toast.error("更新失败: " + (error instanceof Error ? error.message : "未知错误"));
    }
  };

  const handleDeletePrompt = async (id: string) => {
    // 游客模式：使用本地存储
    if (currentUser?.userType === 'GUEST') {
      const updatedPrompts = prompts.filter((p) => p.id !== id);
      setPrompts(updatedPrompts);
      saveLocalPrompts(updatedPrompts);
      setCurrentScreen("main");
      toast.success("Prompt 删除成功！");
      return;
    }

    // 真实用户：调用API
    try {
      await api.prompt.deletePrompt(id);
      setPrompts(prompts.filter((p) => p.id !== id));
      setCurrentScreen("main");
      toast.success("Prompt 删除成功！");
    } catch (error) {
      toast.error("删除失败: " + (error instanceof Error ? error.message : "未知错误"));
    }
  };

  const handleToggleFavorite = async (id: string) => {
    // 游客模式：使用本地存储
    if (currentUser?.userType === 'GUEST') {
      const updatedPrompts = prompts.map((p) =>
        p.id === id ? { ...p, isFavorite: !p.isFavorite, updatedAt: new Date() } : p
      );
      setPrompts(updatedPrompts);
      saveLocalPrompts(updatedPrompts);
      
      // 如果当前正在查看详情，也更新选中的prompt
      if (selectedPrompt?.id === id) {
        const updatedPrompt = updatedPrompts.find((p) => p.id === id);
        if (updatedPrompt) setSelectedPrompt(updatedPrompt);
      }
      
      const prompt = updatedPrompts.find((p) => p.id === id);
      toast.success(prompt?.isFavorite ? "已添加到收藏" : "已取消收藏");
      return;
    }

    // 真实用户：调用API
    try {
      const updatedPrompt = await api.prompt.toggleFavorite(id);
      const converted = convertApiPrompt(updatedPrompt);
      
      setPrompts(prompts.map((p) => (p.id === id ? converted : p)));
      
      // 如果当前正在查看详情，也更新选中的prompt
      if (selectedPrompt?.id === id) {
        setSelectedPrompt(converted);
      }
      
      toast.success(updatedPrompt.isFavorite ? "已添加到收藏" : "已取消收藏");
    } catch (error) {
      toast.error("操作失败: " + (error instanceof Error ? error.message : "未知错误"));
    }
  };

  const handleUsePrompt = async (id: string) => {
    // 增加使用次数
    const updatedPrompts = prompts.map((p) =>
      p.id === id ? { ...p, usageCount: p.usageCount + 1 } : p
    );
    setPrompts(updatedPrompts);
    
    // 游客模式：保存到本地
    if (currentUser?.userType === 'GUEST') {
      saveLocalPrompts(updatedPrompts);
    }
    // 真实用户：可以调用API更新使用次数（如果后端有此接口）
  };

  const handleLogout = async () => {
    try {
      await api.auth.logout();
      setIsLoggedIn(false);
      setCurrentUser(null);
      setCurrentScreen("login");
      setPrompts([]);
      toast.success("已退出登录");
    } catch (error) {
      toast.error("退出失败: " + (error instanceof Error ? error.message : "未知错误"));
    }
  };

  // 根据文件夹ID获取文件夹名称
  const getFolderName = (folderId: string | null): string => {
    if (!folderId) return '';
    
    // 游客模式：文件夹ID就是路径，直接返回最后一部分
    if (currentUser?.userType === 'GUEST') {
      return folderId.split('/').pop() || folderId;
    }
    
    // 真实用户：从文件夹列表中查找
    const folder = folders.find(f => f.id === folderId);
    return folder?.name || folderId;
  };

  // 根据筛选类型获取筛选后的prompts
  const getFilteredPrompts = (): Prompt[] => {
    let filtered = [...prompts];

    // 根据筛选类型进行筛选
    switch (filterType) {
      case 'favorites':
        filtered = filtered.filter(p => p.isFavorite);
        break;
      case 'recent':
        // 按更新时间排序，显示最近更新的
        filtered = [...filtered].sort((a, b) => b.updatedAt.getTime() - a.updatedAt.getTime());
        break;
      case 'all':
      default:
        // 全部，不需要额外筛选
        break;
    }

    // 文件夹筛选
    if (selectedFolder) {
      filtered = filtered.filter(p => p.folder === selectedFolder);
    }

    return filtered;
  };

  // 初始加载中
  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="mb-4 inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-blue-600 border-r-transparent"></div>
          <p className="text-gray-600">加载中...</p>
        </div>
      </div>
    );
  }

  if (!isLoggedIn) {
    return (
      <>
        <LoginScreen onLogin={handleLogin} />
        <Toaster position="top-center" richColors />
      </>
    );
  }

  return (
    <>
      <div className="flex h-screen bg-gray-50">
        {/* Sidebar */}
        <Sidebar
          prompts={prompts}
          folders={folders}
          currentView={currentView}
          selectedFolder={selectedFolder}
          filterType={filterType}
          onViewChange={(view) => {
            setCurrentView(view);
            setCurrentScreen(view === "main" ? "main" : view);
            // 重置筛选状态
            setFilterType('all');
          }}
          onFolderSelect={(folder) => {
            setSelectedFolder(folder);
            // 重置筛选状态
            setFilterType('all');
          }}
          onFilterChange={(type) => {
            setCurrentView("main");
            setCurrentScreen("main");
            setFilterType(type);
            setSelectedFolder(null);
          }}
          onCreateClick={() => setCurrentScreen("create")}
        />

        {/* Main Content */}
        <div className="flex-1 overflow-hidden">
          {currentScreen === "main" && (
            <MainScreen
              prompts={getFilteredPrompts()}
              selectedFolder={selectedFolder}
              selectedFolderName={getFolderName(selectedFolder)}
              filterType={filterType}
              onPromptClick={(prompt) => {
                setSelectedPrompt(prompt);
                setCurrentScreen("detail");
              }}
              onToggleFavorite={handleToggleFavorite}
              isLoading={isLoadingPrompts}
            />
          )}

          {currentScreen === "create" && (
            <CreatePromptScreen
              onSave={handleCreatePrompt}
              onCancel={() => setCurrentScreen("main")}
              onFolderCreated={loadFolders}
              existingPrompts={prompts}
            />
          )}

          {currentScreen === "edit" && selectedPrompt && (
            <CreatePromptScreen
              prompt={selectedPrompt}
              onSave={handleEditPrompt}
              onCancel={() => setCurrentScreen("detail")}
              existingPrompts={prompts}
            />
          )}

          {currentScreen === "detail" && selectedPrompt && (
            <PromptDetailScreen
              prompt={selectedPrompt}
              onBack={() => setCurrentScreen("main")}
              onEdit={() => setCurrentScreen("edit")}
              onDelete={handleDeletePrompt}
              onToggleFavorite={handleToggleFavorite}
              onUse={handleUsePrompt}
            />
          )}

          {currentScreen === "search" && (
            <SearchScreen
              prompts={prompts}
              onBack={() => {
                setCurrentScreen("main");
                setCurrentView("main");
              }}
              onPromptClick={(prompt) => {
                setSelectedPrompt(prompt);
                setCurrentScreen("detail");
              }}
            />
          )}

          {currentScreen === "profile" && currentUser && (
            <ProfileScreen
              onBack={() => {
                setCurrentScreen("main");
                setCurrentView("main");
              }}
              userId={currentUser.userId}
              userName={currentUser.displayName}
              userAvatar=""
              loginType={currentUser.userType.toLowerCase() as "registered" | "guest" | "oauth"}
              onLogout={handleLogout}
            />
          )}
        </div>
      </div>
      <Toaster position="top-center" richColors />
    </>
  );
}
