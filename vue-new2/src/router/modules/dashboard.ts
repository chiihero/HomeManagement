export default {
  path: "/dashboard",
  redirect: "/dashboard/index",
  meta: {
    title: "仪表盘",
  },
  children: [
    {
      path: "/dashboard/index",
      name: "Dashboard",
      component: () => import("@/views/dashboard/index.vue"),
      meta: {
        title: "仪表盘",
      }
    }
  ]
};
