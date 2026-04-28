import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: "/poster-generator",
    },
    {
      path: "/poster-generator",
      name: "poster-generator",
      component: () => import("@/views/poster-generator/index.vue"),
    },
  ],
});

export default router;
