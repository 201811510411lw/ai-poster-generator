import { createRouter, createWebHistory } from "vue-router";
import { hasStoredAuth } from "@/utils/authStorage";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "login",
      component: () => import("@/views/login/index.vue"),
    },
    {
      path: "/poster-generator",
      name: "poster-generator",
      component: () => import("@/views/poster-generator/index.vue"),
      meta: {
        requiresAuth: true,
      },
    },
  ],
});

router.beforeEach((to) => {
  const isAuthed = hasStoredAuth();

  if (to.meta.requiresAuth && !isAuthed) {
    return {
      path: "/login",
      query: {
        redirect: to.fullPath,
      },
    };
  }

  if (to.path === "/login" && isAuthed) {
    const redirect = to.query.redirect;
    return typeof redirect === "string" && redirect !== "/login" ? redirect : "/poster-generator";
  }

  return true;
});

export default router;
