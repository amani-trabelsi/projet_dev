<script lang="ts">
import { defineComponent } from 'vue';
import type { UserType } from '../user'; 
import { api } from '../http-api';


export default defineComponent({
  name: 'SignupPage',
  data() {
    return {
      // Initialize user with UserType
      user: {
        name: '',
        email: '',
        password: '',
      } as UserType,
      nameError: '',
      emailError: '',
      passwordError: '',
      loading: false,
      signupSuccess: '',
    };
  },
  methods: {
    // Handle signup
    async handleSignup() {
      this.loading = true;
      this.resetErrors();

      let hasError = this.validateInputs();
      if (hasError) {
        this.loading = false;
        return;
      }

      try {
        const response = await this.signupApi(this.user);
        console.log('Signup success:', response);
        
        if (response) { 
          localStorage.setItem('user', JSON.stringify(response));
          this.signupSuccess = "Account created successfully!";
          setTimeout(() => {
            this.$router.push('/');
          }, 2000);
          window.location.reload();
        } else {
          throw new Error("Signup failed");
        }
      } catch (error) {
        console.error('Signup failed:', error);
      } finally {
        this.loading = false;
      }
    },
    
    validateInputs() {
      let hasError = false;
      
      if (!this.user.name) {
        this.nameError = 'Full name is required';
        hasError = true;
      }
      
      if (!this.user.email) {
        this.emailError = 'Email is required';
        hasError = true;
      } else if (!this.isValidEmail(this.user.email)) {
        this.emailError = 'Please enter a valid email address';
        hasError = true;
      }
      
      if (!this.user.password) {
        this.passwordError = 'Password is required';
        hasError = true;
      } else if (this.user.password.length < 8) {
        this.passwordError = 'Password must be at least 8 characters';
        hasError = true;
      }
      
      return hasError;
    },
    
    isValidEmail(email: string) {
      const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      return re.test(String(email).toLowerCase());
    },
    
    resetErrors() {
      this.nameError = '';
      this.emailError = '';
      this.passwordError = '';
    },
    
    async signupApi(user: UserType) {
        try {
            const result=await api.signup(user.email, user.password,user.name);
            console.log('signup success:', result);
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
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900">Create your account</h2>
        <p class="mt-2 text-sm text-gray-600">Join us today and get started</p>
      </div>

      <div class="text-center mt-4" v-if="signupSuccess">
        <div class="p-4 text-green-800 bg-green-100 border border-green-400 rounded-lg">
          {{ signupSuccess }}
        </div>
      </div>

      <form class="mt-8 space-y-6" @submit.prevent="handleSignup">
        <div class="rounded-md shadow-sm space-y-4">
          <div>
            <label for="full-name" class="sr-only">Full name</label>
            <input
              id="full-name"
              type="text"
              v-model="user.name"
              placeholder="Full name"
              autocomplete="name"
              class="appearance-none relative block w-full px-3 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-md focus:outline-none focus:ring-purple-500 focus:border-purple-500 focus:z-10 sm:text-sm"
              :class="{'border-red-500 ring-1 ring-red-500': nameError}"
            />
            <p v-if="nameError" class="mt-1 text-sm text-red-600">{{ nameError }}</p>
          </div>
          
          <div>
            <label for="email-address" class="sr-only">Email address</label>
            <input
              id="email-address"
              type="email"
              v-model="user.email"
              placeholder="Email address"
              autocomplete="email"
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
              autocomplete="new-password"
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
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-purple-500 group-hover:text-purple-400" viewBox="0 0 20 20" fill="currentColor">
                <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6z" />
              </svg>
            </span>
            <span v-if="loading">Creating account...</span>
            <span v-else>Sign up</span>
          </button>
        </div>
      </form>
      
      <div class="text-center mt-6">
        <p class="text-sm text-gray-600">
          Already have an account?
          <router-link to="/login" class="font-medium text-purple-600 hover:text-purple-500">Sign In</router-link>
        </p>
      </div>
    </div>
  </div>
</template>