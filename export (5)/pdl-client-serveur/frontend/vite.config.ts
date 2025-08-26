import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: 4000,
    proxy: {
      '^/images': {
        target: 'http://localhost:8686' // Spring boot backend address
      }
    }
  },
  build: {
    outDir: 'target/dist',
    assetsDir: 'static'
  },  
  plugins: [vue(), vueDevTools({launchEditor: 'code',})],
})
