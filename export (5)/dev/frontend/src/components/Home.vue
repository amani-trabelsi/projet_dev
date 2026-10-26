<template>
  <div class=" relative overflow-hidden">
    <div class="flex flex-col items-center justify-center py-20 px-4 z-10 relative">
      <section class="text-center">
        <h1 class="text-5xl sm:text-6xl font-extrabold text-gray-800 mb-6 drop-shadow-lg">
          Welcome to <span class="text-purple-600">My Gallery</span>
        </h1>
        <p class="text-lg sm:text-xl text-gray-600 mb-8 max-w-xl mx-auto">
          Smart tools. Safe storage. Beautiful gallery. Everything you need to manage your images with ease.
        </p>
        <div class="flex justify-center gap-4">
          <router-link
            to="/gallery"
            class="px-6 py-3 rounded-full bg-purple-600 text-white font-semibold hover:bg-purple-700 transition-all shadow-md"
          >
            View Gallery
          </router-link>
          <router-link
            to="/upload"
            class="px-6 py-3 rounded-full bg-white text-purple-600 border border-purple-600 font-semibold hover:bg-purple-100 transition-all shadow-md"
          >
            Upload Image
          </router-link>
        </div>
      </section>

      <section class="mt-20 w-full max-w-5xl grid sm:grid-cols-2 md:grid-cols-3 gap-6 px-6">
        <div class="bg-white p-6 rounded-xl shadow-md hover:shadow-xl transition-all text-center">
          <h3 class="text-xl font-semibold text-purple-700 mb-2">Visual Similarity</h3>
          <p class="text-gray-600 text-sm">Easily find similar images and keep your collections organized.</p>
        </div>
        <div class="bg-white p-6 rounded-xl shadow-md hover:shadow-xl transition-all text-center">
          <h3 class="text-xl font-semibold text-purple-700 mb-2">Secure</h3>
          <p class="text-gray-600 text-sm">Your favorite images are stored securely and only accessible to you.</p>
        </div>
        <div class="bg-white p-6 rounded-xl shadow-md hover:shadow-xl transition-all text-center">
          <h3 class="text-xl font-semibold text-purple-700 mb-2">Fast Uploads</h3>
          <p class="text-gray-600 text-sm">Add new images instantly with a seamless interface.</p>
        </div>
      </section>
    </div>

    <div class="absolute top-[-5rem] left-[-5rem] w-80 h-80 bg-purple-300 opacity-20 rounded-full mix-blend-multiply blur-3xl animate-pulse"></div>
    <div class="absolute bottom-[-5rem] right-[-5rem] w-80 h-80 bg-indigo-300 opacity-20 rounded-full mix-blend-multiply blur-3xl animate-pulse"></div>
  </div>
</template>

