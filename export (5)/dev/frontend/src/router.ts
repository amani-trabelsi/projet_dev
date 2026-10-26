import { createWebHistory, createRouter } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "home",
    component: () => import("./components/Home.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/gallery",
    name: "gallery",
    component: () => import("./components/Gallery.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/favorites",
    name: "favorites",
    component: () => import("./components/Favorites.vue"),
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/login",
    name: "login",
    component: () => import("./components/Login.vue"),
    props: true,
    meta: { requiresGuest: true }
  },
  {
    path: "/signup",
    name: "signup",
    component: () => import("./components/SignUp.vue"),
    props: true,
    meta: { requiresGuest: true }
  },
  {
    path: "/image/:id",
    name: "image",
    component: () => import("./components/Image.vue"),
    props: ({ params }) => ({ id: Number(params.id) || 0 }),
    meta: { requiresAuth: true }
  },
  {
    path: "/upload",
    name: "upload",
    component: () => import("./components/Upload.vue"),
    props: true,
    meta: { requiresAuth: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const isAuthenticated = localStorage.getItem('user');
  if (to.meta.requiresGuest && isAuthenticated) { 
    next('/');
    return;
  }
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login');
    return;
  }
  next();
});
export default router;
