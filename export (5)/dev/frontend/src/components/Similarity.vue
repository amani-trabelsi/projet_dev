<script setup lang="ts">
import { ref } from 'vue';
import { api } from '../http-api';
import type { ImageType } from '../image';

const props = defineProps({
  image: Object as () => ImageType | null,
});

const emit = defineEmits(['close', 'select-similar']); // Ajout du nouvel emit

const similarImages = ref<ImageType[]>([]);
const number = ref(2); 
const descriptor = ref('histoRGB'); 
const isLoading = ref(false);

const getImageSrc = (image: ImageType) => {
  return `data:${image.type};base64,${image.bytes}`;
};

const fetchSimilarImages = async () => {
  if (props.image) {
    isLoading.value = true;
    try {
      const response = await api.getSimilarImages(props.image.id, number.value, descriptor.value); 
      if (response && Array.isArray(response)) {
        similarImages.value = response; 
        similarImages.value.sort((a,b)=>a.score-b.score).reverse();
      } else {
        console.error('No similar images found in the response.');
        similarImages.value = [];
      }
    } catch (error) {
      console.error('Error fetching similar images:', error);
      similarImages.value = [];
    } finally {
      isLoading.value = false;
    }
  }
};

// Nouvelle fonction pour gérer la sélection d'une image similaire
const handleSelectSimilarImage = (image: ImageType) => {
  emit('select-similar', image);
  emit('close');
};
</script>

<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
    <div class="bg-white p-6 rounded-lg shadow-lg w-11/12 max-w-4xl relative overflow-auto max-h-[95vh]">
      <button @click="$emit('close')" class="absolute top-4 right-4 text-gray-600 hover:text-gray-900 text-xl">&times;</button>
      
      <div v-if="props.image" class="mb-4">
        <h2 class="text-xl font-semibold mb-4">Selected Image</h2>
        <img :src="getImageSrc(props.image)" alt="Selected Image" class="w-full h-auto max-h-96 object-contain rounded-lg" />
      </div>
      
      <div class="mb-4">
        <form @submit.prevent="fetchSimilarImages">
          <div class="mb-4">
            <label for="number" class="block text-sm font-medium text-gray-700">Number of Similar Images</label>
            <input 
              v-model="number" 
              type="number" 
              id="number" 
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              min="1"
              required
            />
          </div>
          <div class="mb-4">
            <label for="descriptor" class="block text-sm font-medium text-gray-700">Descriptor</label>
            <select 
              v-model="descriptor" 
              id="descriptor" 
              class="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              required
            >
              <option value="histoRGB">histoRGB</option>
              <option value="histoTeinteSaturation">histoTeinteSaturation</option>
              <option value="combined">combined</option>
            </select>
          </div>
          <div class="flex justify-end">
            <button 
              type="button" 
              @click="fetchSimilarImages" 
              :disabled="isLoading"
              class="px-6 py-2 bg-blue-500 text-white rounded-md shadow-md hover:bg-blue-700 disabled:opacity-50"
            >
              <span v-if="!isLoading">Search Similar Images</span>
              <span v-else>Loading...</span>
            </button>
          </div>
        </form>
      </div>
      
      <div v-if="isLoading" class="flex justify-center my-4">
        <span class="loader"></span>
      </div>
      
      <div v-if="similarImages.length && !isLoading">
        <h3 class="text-lg font-semibold mb-4">Similar Images</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div 
            v-for="image in similarImages" 
            :key="image.id" 
            class="group relative cursor-pointer overflow-hidden rounded-lg shadow-md hover:shadow-xl transform hover:scale-105 transition duration-300 bg-white"
            @click="handleSelectSimilarImage(image)"
          >
            <img :src="getImageSrc(image)" alt="Similar Image" class="w-full h-40 object-cover" />
            <div class="absolute bottom-0 left-0 right-0 bg-black bg-opacity-60 text-white text-sm p-2 text-center">
              Score: {{ (image.score * 100).toFixed(2) }}%
            </div>
            <div class="absolute inset-0 bg-black bg-opacity-30 opacity-0 group-hover:opacity-100 transition duration-300 flex items-center justify-center text-white text-lg font-semibold">
              View Details
            </div>
          </div>
        </div>
      </div>

      <div class="mt-4 flex justify-end">
        <button 
          @click="$emit('close')" 
          class="px-6 py-2 bg-gray-500 text-white rounded-md shadow-md hover:bg-gray-700"
        >
          Close
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fixed {
  overflow-y: auto;
}

.loader {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #3498db;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>