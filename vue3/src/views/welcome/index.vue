<script setup lang="ts">
defineOptions({
  name: "Welcome"
});

const updateNotes = [
  {
    version: "v0.1.3",
    date: "2025年4月14日",
    notes: [
      "优化token管理",
      "优化用户信息管理",
      "完善物品编辑删除图片功能",
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
  <div class="welcome-container">
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
.welcome-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

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
