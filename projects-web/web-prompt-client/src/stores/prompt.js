import { defineStore } from 'pinia'

export const usePromptStore = defineStore('prompt', {
  state: () => ({
    prompts: [],
    currentPrompt: null,
    searchQuery: '',
    selectedTags: [],
    pagination: {
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0
    },
    loading: false
  }),

  getters: {
    getPrompts: (state) => state.prompts,
    getCurrentPrompt: (state) => state.currentPrompt,
    getSearchQuery: (state) => state.searchQuery,
    getSelectedTags: (state) => state.selectedTags,
    getPagination: (state) => state.pagination,
    isLoading: (state) => state.loading
  },

  actions: {
    setPrompts(prompts) {
      this.prompts = prompts
    },

    setCurrentPrompt(prompt) {
      this.currentPrompt = prompt
    },

    setSearchQuery(query) {
      this.searchQuery = query
    },

    setSelectedTags(tags) {
      this.selectedTags = tags
    },

    setPagination(pagination) {
      this.pagination = pagination
    },

    setLoading(loading) {
      this.loading = loading
    },

    addPrompt(prompt) {
      this.prompts.unshift(prompt)
    },

    updatePrompt(updatedPrompt) {
      const index = this.prompts.findIndex(p => p.id === updatedPrompt.id)
      if (index !== -1) {
        this.prompts.splice(index, 1, updatedPrompt)
      }
      if (this.currentPrompt && this.currentPrompt.id === updatedPrompt.id) {
        this.currentPrompt = updatedPrompt
      }
    },

    removePrompt(promptId) {
      this.prompts = this.prompts.filter(p => p.id !== promptId)
      if (this.currentPrompt && this.currentPrompt.id === promptId) {
        this.currentPrompt = null
      }
    },

    toggleFavorite(promptId) {
      const prompt = this.prompts.find(p => p.id === promptId)
      if (prompt) {
        prompt.isFavorite = !prompt.isFavorite
      }
      if (this.currentPrompt && this.currentPrompt.id === promptId) {
        this.currentPrompt.isFavorite = !this.currentPrompt.isFavorite
      }
    },

    clearCurrentPrompt() {
      this.currentPrompt = null
    },

    clearFilters() {
      this.searchQuery = ''
      this.selectedTags = []
      this.pagination.page = 0
    }
  }
})
