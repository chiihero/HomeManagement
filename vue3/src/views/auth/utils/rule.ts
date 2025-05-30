import { reactive } from "vue";
import type { FormRules } from "element-plus";

/** 密码正则（密码格式应为8-18位数字、字母、符号的任意两种组合） */
export const REGEXP_PWD =
  /^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?!([^(0-9a-zA-Z)]|[()])+$)(?!^.*[\u4E00-\u9FA5].*$)([^(0-9a-zA-Z)]|[()]|[a-z]|[A-Z]|[0-9]){8,18}$/;

/** 登录校验 */
const loginRules = reactive(<FormRules>{
  username: [
    {
      required: true,
      message: "请输入用户名",
      trigger: "blur"
    },
    {
      min: 3,
      max: 20,
      message: "长度在 3 到 20 个字符",
      trigger: "blur"
    }
  ],
  password: [
    {
      required: true,
      message: "请输入密码",
      trigger: "blur"
    },
    {
      min: 6,
      max: 20,
      message: "长度在 6 到 20 个字符",
      trigger: "blur"
    }
  ]
});

export { loginRules };
