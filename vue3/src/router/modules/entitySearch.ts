export default {
  path: "/entitySearch",
  redirect: "/entitySearch/index",
  meta: {
    title: "物品搜索",
    icon: "ep:search"
  },
  children: [
    {
      path: "/entitySearch/index",
      name: "EntitySearch",
      component: () => import("@/views/entitySearch/index.vue"),
      meta: {
        title: "物品搜索"
      }
    },
    {
      path: "/entitySearch/view/:id",
      name: "EntitySearchView",
      component: () => import("@/views/entitySearch/EntityView.vue"),
      meta: {
        title: "物品详情",
        hideInMenu: true
      }
    },
    {
      path: "/entitySearch/edit/:id",
      name: "EntitySearchEdit",
      component: () => import("@/views/entitySearch/EntityEdit.vue"),
      meta: {
        title: "编辑物品",
        hideInMenu: true
      }
    }
  ]
};
