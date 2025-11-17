// API 类型定义

// ========== 通用类型 ==========
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageableResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: any;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

// ========== 用户相关类型 ==========
export type UserType = "REGISTERED" | "GUEST" | "OAUTH";
export type DeviceType = "WEB" | "ANDROID" | "IOS";
export type OAuthProvider = "GITHUB" | "GOOGLE" | "WECHAT" | "APPLE";

export interface User {
  userId: string;
  username: string;
  email?: string;
  displayName: string;
  userType: UserType;
  roles: string[];
  avatarUrl?: string;
}

export interface DeviceInfo {
  deviceId: string;
  deviceType: DeviceType;
  os: string;
  browser?: string;
  appVersion: string;
}

// ========== 认证相关类型 ==========
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  displayName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface OAuthCallbackRequest {
  providerUserId: string;
  email?: string;
  username?: string;
  avatarUrl?: string;
  profileData?: Record<string, any>;
  accessToken: string;
  refreshToken?: string;
  expiresAt?: string;
}

export interface OAuthConnection {
  provider: OAuthProvider;
  providerUserId: string;
  createdAt: string;
}

// ========== 会话相关类型 ==========
export interface CreateSessionRequest {
  deviceInfo: DeviceInfo;
  tokenExpiryHours?: number;
}

export interface Session {
  sessionId: string;
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  deviceInfo: DeviceInfo;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}

export interface SessionStats {
  totalSessions: number;
  activeSessions: number;
  deviceTypes: Record<DeviceType, number>;
  lastActivity: string;
}

// ========== Prompt相关类型 ==========
export type PromptStatus = "ACTIVE" | "INACTIVE" | "DELETED";

export interface Prompt {
  id: string;
  userId: string;
  title: string;
  content: string;
  description?: string;
  tags: string[];
  category: string;
  isPublic: boolean;
  isFavorite: boolean;
  usageCount: number;
  folderId?: string;
  status: PromptStatus;
  lastUsedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePromptRequest {
  title: string;
  content: string;
  description?: string;
  tags: string[];
  category: string;
  isPublic?: boolean;
  folderId?: string;
}

export interface UpdatePromptRequest {
  title?: string;
  content?: string;
  description?: string;
  tags?: string[];
  category?: string;
  isPublic?: boolean;
  isFavorite?: boolean;
  folderId?: string;
  status?: PromptStatus;
}

export interface PromptQueryParams {
  page?: number;
  size?: number;
  search?: string;
  tags?: string[];
  category?: string;
  isFavorite?: boolean;
}

export interface PromptStats {
  totalPrompts: number;
  favoritePrompts: number;
  publicPrompts: number;
  totalUsage: number;
  mostUsedTags: string[];
}

// ========== 会员相关类型 ==========
export type PlanType = "FREE" | "PREMIUM" | "ENTERPRISE";
export type MembershipStatus = "ACTIVE" | "CANCELLED" | "EXPIRED";
export type BillingCycle = "MONTHLY" | "YEARLY";

export interface MembershipFeatures {
  promptLimit: number;
  storageLimit: number;
  exportEnabled: boolean;
  prioritySupport?: boolean;
}

export interface UsageLimits {
  prompts: number;
  storage: number;
}

export interface CurrentUsage {
  prompts: number;
  storage: number;
}

export interface Membership {
  membershipId: string;
  userId: string;
  planType: PlanType;
  status: MembershipStatus;
  features: MembershipFeatures;
  usageLimits: UsageLimits;
  currentUsage: CurrentUsage;
  currentPeriodStart: string;
  currentPeriodEnd: string;
}

export interface UpgradeMembershipRequest {
  planId: string;
  planType: PlanType;
  billingCycle: BillingCycle;
  amount: number;
}

export interface UpdateUsageRequest {
  feature: string;
  usage: number;
}

export interface MembershipStats {
  totalUsage: number;
  promptUsage: number;
  storageUsage: number;
  remainingPrompts: number;
  remainingStorage: number;
}

// ========== 错误类型 ==========
export interface ApiError {
  code: string;
  message: string;
  data?: any;
}
