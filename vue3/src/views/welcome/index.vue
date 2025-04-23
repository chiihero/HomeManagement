<script setup lang="ts">
defineOptions({
  name: "Welcome"
});

const updateNotes = [{
    version: "v0.3.4",
    date: "2025年4月23日",
    notes: ["完善图标显示"
    ]
  },{
    version: "v0.3.3",
    date: "2025年4月22日",
    notes: ["优化系统细节",
    "实现点击分类物品树的具体内容之后，添加物品时候的所属位置要对应起来分类物品树的内容"
    ]
  },
{
    version: "v0.3.2",
    date: "2025年4月19日",
    notes: ["精简项目组件，优化echarts、 element-plus的引用，将体积减少50%"
    ]
  },
{
    version: "v0.3.1",
    date: "2025年4月18日",
    notes: ["完善压缩代码",
    "添加个人资料修改页面",
    "添加头像上传功能"
    ]
  },
  {
    version: "v0.3.0",
    date: "2025年4月17日",
    notes: ["通过libavif实现图片高效压缩成avif格式"]
  },
  {
    version: "v0.2.1",
    date: "2025年4月16日",
    notes: ["添加物品数量属性"]
  },
  {
    version: "v0.2.0",
    date: "2025年4月16日",
    notes: [
      "重构前端功能",
      "1.将常用功能函数分离到单独文件夹",
      "2.将图片改为url显示，后端图片改为保存在文件系统"
    ]
  },
  {
    version: "v0.1.4",
    date: "2025年4月15日",
    notes: ["完善仪表盘功能和数据显示"]
  },
  {
    version: "v0.1.3",
    date: "2025年4月14日",
    notes: [
      "优化token管理",
      "优化用户信息管理",
      "完善物品管理图片加载、上传和删除功能",
      "完善代码注释"
    ]
  },
  {
    version: "v0.1.2",
    date: "2025年4月14日",
    notes: ["优化物品管理的表单选项样式"]
  },
  {
    version: "v0.1.1",
    date: "2025年4月14日",
    notes: ["优化axios请求"]
  },
  {
    version: "v0.1.0",
    date: "2025年4月13日",
    notes: [
      "新增物品搜索的查看详情页面",
      "新增物品搜索的物品编辑页面",
      "删除mock数据",
      "优化代码"
    ]
  }
];
</script>

<template>
  <div class="p-4">
    <h1 style="text-align: center">家庭物品管理系统</h1>

    <div class="update-notes">
      <h2>更新说明</h2>
      <div
        v-for="(update, index) in updateNotes"
        :key="index"
        class="update-item"
      >
        <div class="update-header">
          <span class="version">{{ update.version }}</span>
          <span class="date">{{ update.date }}</span>
        </div>
        <ul class="notes-list">
          <li v-for="(note, noteIndex) in update.notes" :key="noteIndex">
            {{ note }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>

.update-notes {
  margin-top: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  background-color: #f9f9f9;
}

h2 {
  margin-top: 0;
  color: #333;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

.update-item {
  margin-bottom: 20px;
}

.update-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.version {
  font-weight: bold;
  color: #1976d2;
}

.date {
  color: #666;
}

.notes-list {
  margin: 5px 0 0 0;
  padding-left: 20px;
}

.notes-list li {
  margin-bottom: 5px;
}
</style>
