export default {
  path: "/reminder",
  redirect: "/reminder/index",
  meta: {
    title: "提醒管理",
  },
  children: [
    {
      path: "/reminder/index",
      name: "Reminder",
      component: () => import("@/views/reminder/index.vue"),
      meta: {
        title: "提醒管理",
      }
    }
  ]
}satisfies RouteConfigsTable;
