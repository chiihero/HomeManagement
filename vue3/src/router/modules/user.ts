import { $t } from "@/plugins/i18n";
import type { RouteConfigsTable } from "@/types/router";

const Layout = () => import("@/layout/index.vue");

const userRouter: RouteConfigsTable = {
  path: "/user",
  component: Layout,
  redirect: "/user/profile",
  meta: {
    title: "用户",
    icon: "ri:user-line",
    rank: 99,
    showLink: false, // 隐藏此路由
    roles: ["ADMIN", "USER"]
  },
  children: [
    {
      path: "/user/profile",
      name: "UserProfile",
      component: () => import("@/views/user/profile.vue"),
      meta: {
        title: "个人信息",
        icon: "ri:user-line",
        roles: ["ADMIN", "USER"],
        showLink: false // 隐藏此路由
      }
    }
  ]
};

export default userRouter; 