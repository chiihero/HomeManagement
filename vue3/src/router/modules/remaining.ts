const Layout = () => import("@/layout/index.vue");

export default [
  {
    path: "/auth/login",
    name: "Login",
    component: () => import("@/views/auth/login.vue"),
    meta: {
      title: "登录",
      showLink: false,
      rank: 101
    }
  },
  {
    path: "/auth/register",
    name: "Register",
    component: () => import("@/views/auth/Register.vue"),
    meta: {
      title: "注册",
      showLink: false,
      rank: 102
    }
  },
  {
    path: "/redirect",
    component: Layout,
    meta: {
      title: "加载中...",
      showLink: false,
      rank: 103
    },
    children: [
      {
        path: "/redirect/:path(.*)",
        name: "Redirect",
        component: () => import("@/layout/redirect.vue")
      }
    ]
  }
] satisfies Array<RouteConfigsTable>;
