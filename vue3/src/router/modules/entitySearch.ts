export default {
  path: "/entitySearch",
  redirect: "/entitySearch/index",
  meta: {
    title: "物品搜索",
    icon: "ep:search",
    rank: 3
  },
  children: [
    {
      path: "/entitySearch/index",
      name: "EntitySearch",
      component: () => import("@/views/entitySearch/index.vue"),
      meta: {
        title: "物品搜索"
      }
    }
  ]
};
