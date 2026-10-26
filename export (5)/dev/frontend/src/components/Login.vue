<script lang="ts">
import { defineComponent} from 'vue';
import type { UserType } from '../user'; // Import UserType from user.ts
import { api } from '../http-api';

export default defineComponent({
  name: 'LoginPage',
  data() {
    return {
      user: {
        name: '',
        email: '',
        password: '',
      } as UserType,
      emailError: '',
      passwordError: '',
      loading: false,
      notification: '',
      notificationType: ''
    };
  },
  methods: {
    // Handle login
    async handleLogin() {
      this.loading = true;
      this.emailError = '';
      this.passwordError = '';
      this.notification = '';
      this.notificationType = '';

      if (!this.user.email) {
        this.emailError = 'Email is required';
      }
      if (!this.user.password) {
        this.passwordError = 'Password is required';
      }

      if (this.emailError || this.passwordError) {
        this.loading = false;
        return;
      }

      try {
        const response = await this.loginApi(this.user);
        if (response) {
          localStorage.setItem('user', JSON.stringify(response));
          this.notification = 'Logged in successfully!';
          this.notificationType = 'success';
          setTimeout(() => {
            this.$router.push('/');
          }, 2000);
          window.location.reload();
        } else {
          this.notification = 'Login failed, please try again.';
          this.notificationType = 'error'; 
        }
      } catch (error) {
        console.error('Login failed:', error);
        this.notification = 'An error occurred during login.';
        this.notificationType = 'error';
      } finally {
        this.loading = false;
      }
    },

    async loginApi(user: UserType) {
        try {
            const result=await api.login(user.email,user.password);
            console.log('Login success:', result);
            return result;
        } catch (e: unknown) {
            if (e instanceof Error) {
            console.log(e.message);
            } else {
            console.log('An unknown error occurred');
            return null;
            }
        }
    },
  },
});
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gradient-to-br from-indigo-100 via-white to-purple-100 items-center justify-center">
    <div class="max-w-md w-full space-y-8 bg-white p-8 rounded-lg shadow-md">
      <div class="text-center">
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Sign in to your account</h2>
        <p class="mt-2 text-sm text-gray-600">Enter your credentials to access your account</p>
      </div>
      <div v-if="notification" :class="{'bg-green-100 text-green-800': notificationType === 'success', 'bg-red-100 text-red-800': notificationType === 'error'}" class="p-4 rounded-md mb-4">
        <p>{{ notification }}</p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div class="rounded-md shadow-sm space-y-4">
          <div>
            <label for="email-address" class="sr-only">Email address</label>
            <input
              id="email-address"
              type="email"
              v-model="user.email"
              placeholder="Email address"
              autocomplete="email"
              required
              class="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-purple-500 focus:border-purple-500 focus:z-10 sm:text-sm"
              :class="{'border-red-500 ring-1 ring-red-500': emailError}"
            />
            <p v-if="emailError" class="mt-1 text-sm text-red-600">{{ emailError }}</p>
          </div>
          
          <div>
            <label for="password" class="sr-only">Password</label>
            <input
              id="password"
              type="password"
              v-model="user.password"
              placeholder="Password"
              autocomplete="current-password"
              required
              class="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-purple-500 focus:border-purple-500 focus:z-10 sm:text-sm"
              :class="{'border-red-500 ring-1 ring-red-500': passwordError}"
            />
            <p v-if="passwordError" class="mt-1 text-sm text-red-600">{{ passwordError }}</p>
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3">
              <svg class="h-5 w-5 text-purple-500 group-hover:text-purple-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
              </svg>
            </span>
            <span v-if="loading">Signing in...</span>
            <span v-else>Sign in</span>
          </button>
        </div>
      </form>
      
      <div class="text-center mt-6">
        <p class="text-sm text-gray-600">
          Don't have an account?
          <router-link to="/signup" class="font-medium text-purple-600 hover:text-purple-500">Sign Up</router-link>
        </p>
      </div>
    </div>
  </div>
</template>