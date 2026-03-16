import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import LoveChatView from '../views/LoveChatView.vue';
import ManusChatView from '../views/ManusChatView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/love-chat',
      name: 'love-chat',
      component: LoveChatView,
    },
    {
      path: '/manus-chat',
      name: 'manus-chat',
      component: ManusChatView,
    },
  ],
});

export default router;

