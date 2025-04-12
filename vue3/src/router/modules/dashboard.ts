export default {
  path: "/dashboard",
  redirect: "/dashboard/index",
  meta: {
    title: "仪表盘",
    icon: "ep:odometer"

  },
  children: [
    {
      path: "/dashboard/index",
      name: "Dashboard",
      component: () => import("@/views/dashboard/index.vue"),
      meta: {
        title: "仪表盘"
      }
    }
  ]
};
