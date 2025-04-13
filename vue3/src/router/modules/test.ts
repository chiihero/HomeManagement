export default {
  path: "/test",
  redirect: "/test/index",
  meta: {
<<<<<<< Updated upstream
=======
    showLink: false,
>>>>>>> Stashed changes
    title: "测试"
  },
  children: [
    {
      path: "/test/index",
      name: "Test",
      component: () => import("@/views/test/index.vue"),
      meta: {
        title: "测试"
      }
    }
  ]
};
