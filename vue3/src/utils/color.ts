// 计算对比色，确保文字在背景色上可见
export const getContrastColor = (hexColor: string) => {
    // 如果没有颜色或颜色格式不正确，默认返回黑色
    if (!hexColor || !hexColor.startsWith("#")) {
      return "#000000";
    }

    // 移除#前缀并处理不同格式的颜色（#RGB和#RRGGBB）
    let hex = hexColor.slice(1);
    if (hex.length === 3) {
      hex = hex
        .split("")
        .map(x => x + x)
        .join("");
    }

    // 转换为RGB
    const r = parseInt(hex.substring(0, 2), 16);
    const g = parseInt(hex.substring(2, 4), 16);
    const b = parseInt(hex.substring(4, 6), 16);

    // 计算亮度 (YIQ方程式)
    const yiq = (r * 299 + g * 587 + b * 114) / 1000;

    // 根据亮度返回黑色或白色
    return yiq >= 150 ? "#000000" : "#ffffff";
  };

  
// 将十六进制颜色转换为RGB
export const hexToRgb = (hex: string) => {
    // 处理rgba格式
    if (hex.startsWith('rgba')) {
      const matches = hex.match(/rgba\((\d+),\s*(\d+),\s*(\d+),\s*[\d.]+\)/);
      if (matches) {
        return {
          r: parseInt(matches[1], 10),
          g: parseInt(matches[2], 10),
          b: parseInt(matches[3], 10)
        };
      }
      return null;
    }
    
    // 处理十六进制格式
    const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
    hex = hex.replace(shorthandRegex, (m, r, g, b) => r + r + g + g + b + b);
    
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result
      ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        }
      : null;
  };

  
// 将rgba颜色转换为十六进制
export const rgbaToHex = (rgba: string) => {
    const matches = rgba.match(/rgba\((\d+),\s*(\d+),\s*(\d+),\s*[\d.]+\)/);
    if (matches) {
      const r = parseInt(matches[1], 10);
      const g = parseInt(matches[2], 10);
      const b = parseInt(matches[3], 10);
      return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`;
    }
    return '#409EFF'; // 默认返回蓝色
  };