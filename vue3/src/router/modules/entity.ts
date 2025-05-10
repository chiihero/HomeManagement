export default {
  path: "/entity",
  redirect: "/entity/index",
  meta: {
    icon: "ep:folder",
    title: "物品管理",
    rank: 2

  },
  children: [
    {
      path: "/entity/index",
      name: "Entity",
      component: () => import("@/views/entity/index.vue"),
      meta: {
        title: "物品管理"
      }
    }
  ]
};
