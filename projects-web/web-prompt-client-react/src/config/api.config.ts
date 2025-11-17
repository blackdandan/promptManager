/**
 * API 配置文件
 * 
 * 在这里配置您的后端 API 地址
 */

export const API_CONFIG = {
  // 后端 API 基础地址
  BASE_URL: "http://localhost:8080/api",
  
  // Token 过期时间（小时）
  TOKEN_EXPIRY_HOURS: 24,
  
  // 是否启用游客模式
  ENABLE_GUEST_MODE: true,
  
  // 是否启用Google登录
  ENABLE_GOOGLE_LOGIN: true,
  
  // 是否启用微信登录
  ENABLE_WECHAT_LOGIN: true,
  
  // 每页显示的 Prompt 数量
  DEFAULT_PAGE_SIZE: 20,
};

export default API_CONFIG;
