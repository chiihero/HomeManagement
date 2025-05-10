export default {
    path: "/scan",
    redirect: "/scan/index",
    meta: {
        icon: "ep:camera-filled", // You can choose an appropriate icon
        title: "扫码拍照上传",
        rank: 6

    },
    children: [
        {
            path: "/scan/index",
            name: "ScanUploadPage",
            component: () => import("@/views/scan/index.vue"),
            meta: {
                title: "扫码拍照上传"
            }
        }
    ]
};
