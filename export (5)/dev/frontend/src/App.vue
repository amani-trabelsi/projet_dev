<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from './http-api';
import type { ImageType } from './image';
import router from './router';


// Reactive reference to store image list
const imageList = ref<ImageType[]>([]);
// Reactive reference to track authentication status
const isAuthenticated = ref(!!localStorage.getItem('user')); 

// Load the image list from the backend
const loadImageList = async () => {
  try {
    imageList.value = await api.getImageList();
    console.log("test")
  } catch (e: unknown) {
    if (e instanceof Error) {
      console.log(e.message);
    } else {
      console.log('An unknown error occurred');
    }
  }
};

// Log out the user
const isLogoutConfirmationVisible = ref(false);

const showLogoutConfirmation = () => {
  isLogoutConfirmationVisible.value = true;
};

const confirmLogout = () => {
  localStorage.removeItem('user');
  isAuthenticated.value = false;
  router.push("/login");
  isLogoutConfirmationVisible.value = false;
};

const cancelLogout = () => {
  isLogoutConfirmationVisible.value = false;
};



// Delete an image from the gallery
const deleteImage = async (imageId: number) => {
  try {
    await api.deleteImage(imageId);
    const index = imageList.value.findIndex(image => image.id === imageId);
    if (index !== -1) {
      imageList.value.splice(index, 1);
    }
  } catch (e: unknown) {
    if (e instanceof Error) {
      console.log(e.message);
    } else {
      console.log('An unknown error occurred during delete');
    }
  }
};

// Fetch the image list when the component is mounted
onMounted(() => {
  loadImageList();
});

// Add a new image to the image list
function addImage(newImage: ImageType) {
  imageList.value.push(newImage);
  loadImageList();
}
</script>

<template>
  <div id="app" class="min-h-screen flex flex-col bg-gradient-to-br from-indigo-100 via-white to-purple-100">
    <nav class="bg-purple-950 py-6 shadow-md">
      <div class="container mx-auto px-6 flex justify-between items-center">
        <div class="text-white font-semibold text-2xl">My Gallery</div>
        <ul class="flex space-x-6">
          
          <!-- Show these links only if user is NOT authenticated -->
          <template v-if="!isAuthenticated">
            <li><router-link to="/login" class="text-white hover:text-gray-400 transition-all">Login</router-link></li>
            <li><router-link to="/signup" class="text-white hover:text-gray-400 transition-all">Sign Up</router-link></li>
          </template>
          
          <!-- Show these links only if user IS authenticated -->
          <template v-if="isAuthenticated">
            <li><router-link to="/" class="text-white hover:text-gray-400 transition-all">Home</router-link></li>
            <li><router-link to="/gallery" class="text-white hover:text-gray-400 transition-all">Gallery</router-link></li>
            <li><router-link to="/favorites" class="text-white hover:text-gray-400 transition-all">Favorites</router-link></li>  
            <li><router-link to="/upload" class="text-white hover:text-gray-400 transition-all">Upload</router-link></li>
            <li><button @click="showLogoutConfirmation" class="text-white hover:text-gray-400 transition-all">Logout</button></li>
          </template>
        </ul>
      </div>
    </nav>
    <div v-if="isLogoutConfirmationVisible" class="fixed inset-0 w-screen h-screen bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div class="bg-white p-6 rounded-lg shadow-lg text-center">
              <h2 class="text-lg font-semibold mb-4">Are you sure you want to log out?</h2>
              <div class="flex justify-center space-x-4">
                <button
                  class="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
                  @click="confirmLogout"
                >
                Yes, log out
                </button>
                <button
                  class="px-6 py-2 bg-gray-300 text-black rounded-md hover:bg-gray-500"
                  @click="cancelLogout"
                >
                Cancel
                </button>
              </div>
            </div>
    </div>
    <div class="container mx-auto p-6 flex-1">
      <router-view 
        :imageList="imageList" 
        @addImage="addImage" 
        :loadImageList="loadImageList"
        :onDelete="deleteImage"/> 
    </div>
  </div>
</template>
