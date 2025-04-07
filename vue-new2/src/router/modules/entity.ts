export default {
  path: "/entity",
  redirect: "/entity/index",
  meta: {
    title: "物品管理",
  },
  children: [
    {
      path: "/entity/index",
      name: "Entity",
      component: () => import("@/views/entity/index.vue"),
      meta: {
        title: "物品管理",
      }
    }
  ]
};
