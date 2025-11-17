// API 服务层 - 封装所有HTTP请求
import type {
  ApiResponse,
  PageableResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RefreshTokenRequest,
  OAuthCallbackRequest,
  OAuthProvider,
  OAuthConnection,
  CreateSessionRequest,
  Session,
  SessionStats,
  Prompt,
  CreatePromptRequest,
  UpdatePromptRequest,
  PromptQueryParams,
  PromptStats,
  Membership,
  UpgradeMembershipRequest,
  UpdateUsageRequest,
  MembershipStats,
  User,
  DeviceInfo,
} from "../types/api";
import API_CONFIG from "../config/api.config";

// ========== 配置 ==========
const API_BASE_URL = API_CONFIG.BASE_URL;

// ========== 工具函数 ==========

/**
 * 获取存储的访问令牌
 */
function getAccessToken(): string | null {
  return localStorage.getItem("access_token");
}

/**
 * 获取存储的刷新令牌
 */
function getRefreshToken(): string | null {
  return localStorage.getItem("refresh_token");
}

/**
 * 保存令牌到本地存储
 */
function saveTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem("access_token", accessToken);
  localStorage.setItem("refresh_token", refreshToken);
}

/**
 * 清除存储的令牌
 */
function clearTokens(): void {
  localStorage.removeItem("access_token");
  localStorage.removeItem("refresh_token");
  localStorage.removeItem("user");
  localStorage.removeItem("session_id");
}

/**
 * 获取当前用户ID
 */
function getUserId(): string | null {
  const userStr = localStorage.getItem("user");
  if (!userStr) return null;
  try {
    const user = JSON.parse(userStr);
    return user.userId;
  } catch {
    return null;
  }
}

/**
 * 获取当前会话ID
 */
function getSessionId(): string | null {
  return localStorage.getItem("session_id");
}

/**
 * 保存会话ID
 */
function saveSessionId(sessionId: string): void {
  localStorage.setItem("session_id", sessionId);
}

/**
 * 获取设备信息
 */
function getDeviceInfo(): DeviceInfo {
  // 生成或获取设备ID
  let deviceId = localStorage.getItem("device_id");
  if (!deviceId) {
    deviceId = `web_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    localStorage.setItem("device_id", deviceId);
  }

  return {
    deviceId,
    deviceType: "WEB",
    os: navigator.platform || "Unknown",
    browser: navigator.userAgent,
    appVersion: "1.0.0",
  };
}

/**
 * HTTP请求封装
 */
async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = getAccessToken();
  const userId = getUserId();

  const headers: HeadersInit = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  // 添加认证头
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  // 添加用户ID头
  if (userId) {
    headers["X-User-Id"] = userId;
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });

    const data = await response.json();

    // 如果Token过期，尝试刷新
    if (data.code === "AUTH_002" || response.status === 401) {
      const refreshToken = getRefreshToken();
      if (refreshToken) {
        try {
          const newTokens = await authApi.refreshToken({ refreshToken });
          saveTokens(newTokens.accessToken, newTokens.refreshToken);

          // 重试原请求
          headers["Authorization"] = `Bearer ${newTokens.accessToken}`;
          const retryResponse = await fetch(url, { ...options, headers });
          return await retryResponse.json();
        } catch {
          // 刷新失败，清除令牌并跳转登录
          clearTokens();
          window.location.href = "/";
          throw new Error("Token已过期，请重新登录");
        }
      }
    }

    if (!response.ok && data.code !== 200 && data.code !== 201 && data.code !== 204) {
      throw new Error(data.message || "请求失败");
    }

    return data;
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error("网络请求失败");
  }
}

// ========== 认证接口 ==========
export const authApi = {
  /**
   * 用户注册
   */
  async register(data: RegisterRequest): Promise<User> {
    const response = await request<User>("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 用户登录
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await request<LoginResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    });
    saveTokens(response.data.accessToken, response.data.refreshToken);
    localStorage.setItem("user", JSON.stringify(response.data.user));
    return response.data;
  },

  /**
   * 刷新Token
   */
  async refreshToken(data: RefreshTokenRequest): Promise<LoginResponse> {
    const response = await request<LoginResponse>("/auth/refresh", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 登出
   */
  async logout(): Promise<void> {
    clearTokens();
  },
};

// ========== OAuth接口 ==========
export const oauthApi = {
  /**
   * OAuth回调
   */
  async callback(
    provider: OAuthProvider,
    data: OAuthCallbackRequest
  ): Promise<User> {
    const response = await request<User>(`/oauth/callback/${provider}`, {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 获取OAuth连接列表
   */
  async getConnections(): Promise<OAuthConnection[]> {
    const response = await request<OAuthConnection[]>("/oauth/connections");
    return response.data;
  },

  /**
   * 解绑OAuth
   */
  async unbind(provider: OAuthProvider): Promise<void> {
    await request(`/oauth/connections/${provider}`, {
      method: "DELETE",
    });
  },
};

// ========== 会话接口 ==========
export const sessionApi = {
  /**
   * 创建会话
   */
  async createSession(tokenExpiryHours = 24): Promise<Session> {
    const deviceInfo = getDeviceInfo();
    const response = await request<Session>("/sessions", {
      method: "POST",
      body: JSON.stringify({
        deviceInfo,
        tokenExpiryHours,
      }),
    });
    saveSessionId(response.data.sessionId);
    saveTokens(response.data.accessToken, response.data.refreshToken);
    return response.data;
  },

  /**
   * 刷新会话Token
   */
  async refreshSession(tokenExpiryHours = 24): Promise<Session> {
    const refreshToken = getRefreshToken();
    if (!refreshToken) throw new Error("没有刷新令牌");

    const response = await request<Session>("/sessions/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken, tokenExpiryHours }),
    });
    saveSessionId(response.data.sessionId);
    saveTokens(response.data.accessToken, response.data.refreshToken);
    return response.data;
  },

  /**
   * 获取会话列表
   */
  async getSessions(): Promise<Session[]> {
    const response = await request<Session[]>("/sessions");
    return response.data;
  },

  /**
   * 获取活跃会话
   */
  async getActiveSessions(): Promise<Session[]> {
    const response = await request<Session[]>("/sessions/active");
    return response.data;
  },

  /**
   * 失效单个会话
   */
  async invalidateSession(sessionId: string): Promise<void> {
    await request(`/sessions/${sessionId}`, {
      method: "DELETE",
    });
  },

  /**
   * 失效所有会话
   */
  async invalidateAllSessions(): Promise<void> {
    await request("/sessions", {
      method: "DELETE",
    });
    clearTokens();
  },

  /**
   * 失效其他会话
   */
  async invalidateOtherSessions(): Promise<void> {
    const sessionId = getSessionId();
    await request("/sessions/others", {
      method: "DELETE",
      headers: {
        "X-Session-ID": sessionId || "",
      },
    });
  },

  /**
   * 获取会话统计
   */
  async getStats(): Promise<SessionStats> {
    const response = await request<SessionStats>("/sessions/stats");
    return response.data;
  },
};

// ========== Prompt接口 ==========
export const promptApi = {
  /**
   * 获取Prompt列表
   */
  async getPrompts(
    params: PromptQueryParams = {}
  ): Promise<PageableResponse<Prompt>> {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined) queryParams.append("page", String(params.page));
    if (params.size !== undefined) queryParams.append("size", String(params.size));
    if (params.search) queryParams.append("search", params.search);
    if (params.category) queryParams.append("category", params.category);
    if (params.isFavorite !== undefined)
      queryParams.append("isFavorite", String(params.isFavorite));
    if (params.tags) {
      params.tags.forEach((tag) => queryParams.append("tags", tag));
    }

    const response = await request<PageableResponse<Prompt>>(
      `/prompts?${queryParams}`
    );
    return response.data;
  },

  /**
   * 获取单个Prompt
   */
  async getPrompt(id: string): Promise<Prompt> {
    const response = await request<Prompt>(`/prompts/${id}`);
    return response.data;
  },

  /**
   * 创建Prompt
   */
  async createPrompt(data: CreatePromptRequest): Promise<Prompt> {
    const response = await request<Prompt>("/prompts", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 更新Prompt
   */
  async updatePrompt(id: string, data: UpdatePromptRequest): Promise<Prompt> {
    const response = await request<Prompt>(`/prompts/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 删除Prompt
   */
  async deletePrompt(id: string): Promise<void> {
    await request(`/prompts/${id}`, {
      method: "DELETE",
    });
  },

  /**
   * 切换收藏状态
   */
  async toggleFavorite(id: string): Promise<Prompt> {
    const response = await request<Prompt>(`/prompts/${id}/favorite`, {
      method: "POST",
    });
    return response.data;
  },

  /**
   * 获取公开Prompt列表
   */
  async getPublicPrompts(
    params: PromptQueryParams = {}
  ): Promise<PageableResponse<Prompt>> {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined) queryParams.append("page", String(params.page));
    if (params.size !== undefined) queryParams.append("size", String(params.size));
    if (params.search) queryParams.append("search", params.search);
    if (params.tags) {
      params.tags.forEach((tag) => queryParams.append("tags", tag));
    }

    const response = await request<PageableResponse<Prompt>>(
      `/prompts/public?${queryParams}`
    );
    return response.data;
  },

  /**
   * 获取用户标签列表
   */
  async getTags(): Promise<string[]> {
    const response = await request<string[]>("/prompts/tags");
    return response.data;
  },

  /**
   * 获取用户统计信息
   */
  async getStats(): Promise<PromptStats> {
    const response = await request<PromptStats>("/prompts/stats");
    return response.data;
  },
};

// ========== 会员接口 ==========
export const membershipApi = {
  /**
   * 创建免费会员
   */
  async createFreeMembership(): Promise<Membership> {
    const response = await request<Membership>("/membership/free", {
      method: "POST",
    });
    return response.data;
  },

  /**
   * 获取会员信息
   */
  async getMembership(): Promise<Membership> {
    const response = await request<Membership>("/membership");
    return response.data;
  },

  /**
   * 升级会员
   */
  async upgradeMembership(data: UpgradeMembershipRequest): Promise<Membership> {
    const response = await request<Membership>("/membership/upgrade", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 取消会员
   */
  async cancelMembership(): Promise<Membership> {
    const response = await request<Membership>("/membership/cancel", {
      method: "POST",
    });
    return response.data;
  },

  /**
   * 更新使用量
   */
  async updateUsage(data: UpdateUsageRequest): Promise<Membership> {
    const response = await request<Membership>("/membership/usage", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return response.data;
  },

  /**
   * 检查功能访问权限
   */
  async checkAccess(feature: string): Promise<{ feature: string; hasAccess: boolean }> {
    const response = await request<{ feature: string; hasAccess: boolean }>(
      `/membership/check-access/${feature}`
    );
    return response.data;
  },

  /**
   * 获取使用统计
   */
  async getStats(): Promise<MembershipStats> {
    const response = await request<MembershipStats>("/membership/stats");
    return response.data;
  },
};

// ========== 导出所有API ==========
export const api = {
  auth: authApi,
  oauth: oauthApi,
  session: sessionApi,
  prompt: promptApi,
  membership: membershipApi,
};

export default api;