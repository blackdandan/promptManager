import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    isAuthenticated: false,
    token: null,
    refreshToken: null
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    getIsAuthenticated: (state) => state.isAuthenticated,
    getToken: (state) => state.token
  },

  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
    },

    setAuthenticationStatus(status) {
      this.isAuthenticated = status
    },

    setToken(token) {
      this.token = token
      if (token) {
        localStorage.setItem('accessToken', token)
      } else {
        localStorage.removeItem('accessToken')
      }
    },

    setRefreshToken(refreshToken) {
      this.refreshToken = refreshToken
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken)
      } else {
        localStorage.removeItem('refreshToken')
      }
    },

    login(userData) {
      this.userInfo = userData.user
      this.token = userData.accessToken
      this.refreshToken = userData.refreshToken
      this.isAuthenticated = true
      
      localStorage.setItem('accessToken', userData.accessToken)
      localStorage.setItem('refreshToken', userData.refreshToken)
    },

    logout() {
      this.userInfo = null
      this.token = null
      this.refreshToken = null
      this.isAuthenticated = false
      
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
    },

    initializeFromStorage() {
      const token = localStorage.getItem('accessToken')
      const refreshToken = localStorage.getItem('refreshToken')
      
      if (token) {
        this.token = token
        this.refreshToken = refreshToken
        this.isAuthenticated = true
      }
    }
  }
})
