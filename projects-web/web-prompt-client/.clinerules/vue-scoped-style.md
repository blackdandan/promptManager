# Vue Scoped 样式书写规范

## 规则概述
本规范定义了Vue组件中scoped样式的书写模式，要求使用CSS嵌套语法来创建清晰的层级关系。

## 书写规范

### 1. 基本结构
```css
<style scoped>
.component-container {
  /* 根容器样式 */
  
  .child-element {
    /* 子元素样式 */
    
    .grandchild-element {
      /* 孙子元素样式 */
    }
  }
}
</style>
```

### 2. 层级关系要求
- 使用CSS嵌套语法反映HTML结构
- 每个子元素都在其父元素的嵌套块内定义样式
- 保持层级关系清晰分明

### 3. 伪类和伪元素
```css
.menu-item {
  /* 基础样式 */
  
  &:hover {
    /* 悬停状态 */
  }
  
  &.active {
    /* 激活状态 */
  }
  
  &::before {
    /* 伪元素 */
  }
}
```

### 4. 媒体查询
```css
.component {
  /* 基础样式 */
  
  @media (min-width: 768px) {
    /* 响应式样式 */
  }
}
```

## 示例代码

### 完整示例
```vue
<template>
  <div class="home-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">标题</h2>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  height: 100vh;

  .sidebar {
    width: 240px;
    background: white;

    .sidebar-header {
      padding: 20px;

      .sidebar-title {
        font-size: 18px;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
      }
    }
  }
}
</style>
```

### 交互状态示例
```css
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: #f3f4f6;
    color: #374151;
  }

  &.active {
    background-color: #eff6ff;
    color: #2563eb;
    font-weight: 500;
  }
}
```

## 优势
1. **层级清晰** - CSS结构反映HTML结构
2. **维护性好** - 相关样式集中在一起
3. **可读性强** - 易于理解组件结构
4. **作用域安全** - 使用scoped确保样式隔离

## 注意事项
1. 确保开发环境支持CSS嵌套语法
2. 避免嵌套层级过深（建议不超过4层）
3. 合理使用注释说明复杂样式
4. 保持样式命名语义化
