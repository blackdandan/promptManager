/**
 * 跨浏览器兼容的剪贴板复制工具
 * 使用多种降级方案确保复制功能在各种环境下都能工作
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  // 方法1: 尝试使用现代 Clipboard API
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (err) {
    // Clipboard API 失败，尝试降级方案
  }

  // 方法2: 使用 document.execCommand (降级方案)
  try {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    const successful = document.execCommand('copy');
    document.body.removeChild(textArea);
    
    if (successful) {
      return true;
    }
  } catch (err) {
    console.error('execCommand copy failed:', err);
  }

  // 方法3: 使用 Selection API (最后的降级方案)
  try {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    
    const range = document.createRange();
    range.selectNodeContents(textArea);
    
    const selection = window.getSelection();
    if (selection) {
      selection.removeAllRanges();
      selection.addRange(range);
      
      textArea.setSelectionRange(0, text.length);
      const successful = document.execCommand('copy');
      
      selection.removeAllRanges();
      document.body.removeChild(textArea);
      
      if (successful) {
        return true;
      }
    }
  } catch (err) {
    console.error('Selection API copy failed:', err);
  }

  return false;
}
