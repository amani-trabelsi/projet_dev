<script setup lang="ts">
import { ref, onMounted} from 'vue';
import { api } from '../http-api';
import type { ImageType } from '../image';
import Similarity from './Similarity.vue';

const likedImages = ref<ImageType[]>([]);
const selectedImage = ref<ImageType | null>(null);
const isLiked = ref(true); 
const isDeleteConfirmationVisible = ref(false);
const imageToDelete = ref<ImageType | null>(null);
const showSimilarityModal = ref(false);
const brightnessDelta = ref(0);
const filteredImage = ref<ImageType | null>(null);

const props = defineProps<{ 
  imageList: ImageType[];
  onDelete: (imageId: number) => void; 
}>();

const emit = defineEmits(['delete']);

const getUSERFromLocalStorage = () => {
  const user = localStorage.getItem('user');
  if (user) {
    try {
      const parsedUser = JSON.parse(user);
      return parsedUser;
    } catch (e) {
      console.error('Failed to parse user data:', e);
    }
  }
  return null;
};

const getLikedImages = async () => {
  const user = getUSERFromLocalStorage();
  if (user) {
    try {
      const images = await api.getLikedImages(user.email, user.password);
      likedImages.value = images;
    } catch (error) {
      console.error('Error fetching liked images:', error);
    }
  } else {
    console.error("Email not found in localStorage");
  }
};

const selectImage = (image: ImageType) => {
  selectedImage.value = image;
  isLiked.value = true; 
  filteredImage.value = null; 
};

const deselectImage = () => {
  selectedImage.value = null;
  filteredImage.value = null;
};

const toggleLike = () => {
  if (selectedImage.value) {
    unlikeImageHandler(selectedImage.value.id, selectedImage.value.name);
  }
};

const unlikeImageHandler = (id: number, name: string) => {
  const user = getUSERFromLocalStorage();
  if (user) {
    api.unlikeImage(id, user.email, name, user.password)
      .then(response => {
        console.log("Image unlikée avec succès :", response);
        likedImages.value = likedImages.value.filter(image => image.name !== name);
        deselectImage();
      })
      .catch(error => {
        console.error("Erreur lors de l'unlike de l'image :", error);
      });
  } else {
    console.error("Email non trouvé dans le localStorage");
  }
};

onMounted(() => {
  getLikedImages();
});

const getImageSrc = (image: ImageType | string | null) => {
  if (typeof image === 'string') {
    return `data:;base64,${image}`;
  } else if (image && 'type' in image && 'bytes' in image) {
    return `data:${image.type};base64,${image.bytes}`;
  }
  return '';
};

const downloadImage = (image: ImageType) => {
  const imageUrl = getImageSrc(image);
  const link = document.createElement('a');
  link.href = imageUrl;
  link.download = image.name || 'image';  
  link.click();
};

const showDeleteConfirmation = (image: ImageType) => {
  imageToDelete.value = image;
  isDeleteConfirmationVisible.value = true;
};

const confirmDeleteImage = () => {
  if (imageToDelete.value) {
    props.onDelete(imageToDelete.value.id);
    likedImages.value = likedImages.value.filter(image => image.id !== imageToDelete.value?.id);
    deselectImage();
  }
  isDeleteConfirmationVisible.value = false;
};

const cancelDelete = () => {
  isDeleteConfirmationVisible.value = false; 
};

const showSimilarity = () => {
  if (selectedImage.value) {
    showSimilarityModal.value = true;
  }
};

const closeSimilarity = () => {
  showSimilarityModal.value = false;
};
const applyBrightnessFilter = async () => {
  if (!selectedImage.value) return;
  try {
    const response = await api.adjustBrightness(selectedImage.value.id, brightnessDelta.value);
    console.log('Brightness adjusted:', response);
    filteredImage.value = response;
  } catch (error) {
    console.error('Error adjusting brightness:', error);
  }
};
const handleSimilarImageSelect = (image: ImageType) => {
  selectedImage.value = image;
  showSimilarityModal.value = false;
  filteredImage.value = null;
  brightnessDelta.value = 0;
};
</script>

<template>
  <div class="favorites">
    <div v-if="selectedImage" class="fixed inset-0 bg-black bg-opacity-80 overflow-y-auto p-4" style="z-index: 1000;">
      <div class="relative max-w-4xl w-full mx-auto bg-white p-6 rounded-lg shadow-lg flex flex-col items-center mt-10 mb-10">        <button class="absolute top-4 right-4 text-gray-600 hover:text-gray-900 text-xl" @click="deselectImage">&times;</button>
        <img :src="getImageSrc(selectedImage)" alt="Selected Image" class="w-full max-h-[50vh] object-contain rounded-md shadow-md mb-4" />
        
        <div class="w-full flex flex-col items-center sm:flex-row sm:justify-between text-gray-700 mt-4">
          <div class="text-center sm:text-left">
            <p><strong>Type:</strong> {{ selectedImage.type }}</p>
            <p><strong>Size:</strong> {{ parseInt((selectedImage.bytes.length / 1024).toString()) }} Kb</p>
            <p><strong>Name:</strong> {{ selectedImage.name }}</p>
          </div>
          <div class="mt-4 sm:mt-0 flex space-x-4">
            <button class="px-6 py-2 bg-blue-500 text-white rounded-md shadow-md hover:bg-blue-700" @click="downloadImage(selectedImage)">Download</button>
            <button class="px-6 py-2 bg-red-600 text-white rounded-md shadow-md hover:bg-red-700" @click="showDeleteConfirmation(selectedImage)">Delete</button>
            <button class="px-6 py-2 bg-green-600 text-white rounded-md shadow-md hover:bg-green-700" @click="showSimilarity">Similarity</button>
            <button @click="toggleLike" class="focus:outline-none">
              <svg class="w-6 h-6 transition-colors duration-300" :class="{ 'text-red-500': isLiked, 'text-gray-400': !isLiked }" fill="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd" d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" clip-rule="evenodd"/>
              </svg>
            </button>
          </div>
        </div>
        <div class="flex justify-center items-center">
          <div class="mt-4 w-full max-w-md flex items-center justify-center space-x-2">
            <input 
              type="number" 
              v-model="brightnessDelta" 
              placeholder="Enter brightness delta"
              class="px-4 py-2 border rounded-md"
            />
            <button 
              class="px-6 py-2 bg-yellow-500 text-white rounded-md shadow-md hover:bg-yellow-700"
              @click="applyBrightnessFilter"
            >
              Adjust Brightness
            </button>
          </div>
        </div>
        <img 
          v-if="filteredImage" 
          :src="getImageSrc(filteredImage)" 
          alt="Filtered Image" 
          class="w-full max-h-[50vh] object-contain rounded-md shadow-md mt-4" 
        />
        <div class="w-full mt-6" v-if="showSimilarityModal">
          <Similarity 
      v-if="showSimilarityModal" 
      :image="selectedImage" 
      @close="closeSimilarity"
      @select-similar="handleSimilarImageSelect"
    />
        </div>
      </div>
    </div>
    
    <div v-if="isDeleteConfirmationVisible" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4" style="z-index: 1001;">
      <div class="bg-white p-6 rounded-lg shadow-lg text-center">
        <h2 class="text-lg font-semibold mb-4">Are you sure you want to delete this image ?</h2>
        
        <div class="flex justify-center space-x-4">
          <button class="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700" @click="confirmDeleteImage">yes, delete</button>
          <button class="px-6 py-2 bg-gray-300 text-black rounded-md hover:bg-gray-500" @click="cancelDelete">Cancel</button>
        </div>
      </div>
    </div>
    <div v-if="likedImages.length > 0" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 w-11/12 max-w-6xl mx-auto" style="z-index: 1;">      <div v-for="image in likedImages" :key="image.name" class="group relative cursor-pointer overflow-hidden rounded-lg shadow-md hover:shadow-xl transform hover:scale-105 transition duration-300 bg-white" @click="selectImage(image)">
        <img :src="getImageSrc(image)" alt="Liked Image" class="w-full h-48 object-cover" />
        <div class="absolute inset-0 bg-black bg-opacity-30 opacity-0 group-hover:opacity-100 transition duration-300 flex items-center justify-center text-white text-lg font-semibold">View</div>
      </div>
    </div>
    <p v-else class="text-gray-400 flex flex-col items-center mt-10">
      <svg xmlns="http://www.w3.org/2000/svg" class="w-20 h-20 text-gray-300 mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 3v18h18V3H3z"></path>
        <path d="M8 12h.01"></path>
        <path d="M16 12h.01"></path>
        <path d="M12 16h.01"></path>
      </svg>
      <span class="text-lg font-medium">No liked images yet</span>
      <span class="text-sm text-gray-500">Start liking some images to see them here.</span>
    </p>
  </div>
</template>