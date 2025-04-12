export default {
  path: "/tag",
  redirect: "/tag/index",
  meta: {
    icon: "ep:price-tag",
    title: "标签管理"
  },
  children: [
    {
      path: "/tag/index",
      name: "Tag",
      component: () => import("@/views/tag/index.vue"),
      meta: {
        title: "标签管理"
      }
    }
  ]
}; 