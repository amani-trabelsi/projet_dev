<script setup lang="ts">
import { ref } from 'vue';
import { api } from '../http-api';
import type { ImageType } from '../image';

const target = ref<HTMLInputElement | null>(null);
const successMessage = ref<string | null>(null);
const errorMessage = ref<string | null>(null);

const props = defineProps<{
  loadImageList: () => void; 
  imageList: ImageType[];
}>();
const emit = defineEmits<{
  (event: 'addImage', newImage: ImageType): void;
}>();

function submitFile() {
  if (target.value && target.value.files && target.value.files[0]) {
    const file = target.value.files[0];
    let formData = new FormData();
    formData.append("file", file);
    api.createImage(formData)
      .then((newImage: ImageType) => {
        successMessage.value = "The image has been successfully added!";
        setTimeout(() => {
          successMessage.value = null;
        }, 3000); 
        if (target.value) target.value.value = '';
        emit('addImage', newImage);
        props.loadImageList(); 
      })
      .catch((e) => {
        errorMessage.value = `Error: ${e.message}. Please try another file.`;
        setTimeout(() => {
          errorMessage.value = null;
        }, 3000);
        console.log(e.message);
      });
  }
}

function handleFileUpload(event: Event) {
  target.value = (event.target as HTMLInputElement);
}
</script>

<template>
  <div class="max-w-xl mx-auto bg-gradient-to-br from-white via-indigo-50 to-purple-50 p-8 rounded-2xl shadow-2xl mt-16 border border-indigo-100 ">
    <h3 class="text-3xl font-bold text-gray-800 mb-6 text-center drop-shadow-sm">
      Upload an Image
    </h3>

    <div class="mb-6">
      <input 
        type="file" 
        id="file" 
        ref="target" 
        @change="handleFileUpload" 
        class="w-full py-3 px-4 border border-gray-300 rounded-xl shadow-inner focus:outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-purple-100 file:text-purple-700 hover:file:bg-purple-200 transition-all"
      />
    </div>

    <div>
      <button 
        @click="submitFile"
        class="w-full py-3 px-4 bg-purple-600 text-white rounded-xl hover:bg-purple-700 focus:outline-none focus:ring-2 focus:ring-purple-300 transition duration-300 font-medium shadow-lg"
      >
        Upload
      </button>
    </div>

    <div v-if="successMessage" class="mt-6 p-4 bg-green-100 text-green-700 border border-green-300 rounded-lg text-center transition-opacity duration-500 ease-in-out">
      {{ successMessage }}
    </div>

    <div v-if="errorMessage" class="mt-6 p-4 bg-red-100 text-red-700 border border-red-300 rounded-lg text-center transition-opacity duration-500 ease-in-out">
      {{ errorMessage }}
    </div>
  </div>
</template>

<style scoped>
</style>
