import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 根据模式设置基础路径
  // 生产环境和测试环境都需要使用/sp-api/作为基础路径
  const base = (mode === 'production' || mode === 'test') ? '/sp-api/' : '/';

  return {
    plugins: [vue()],
    // 根据环境设置基础路径
    base: base,
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    build: {
      // 确保资源路径正确
      assetsDir: 'assets',
      rollupOptions: {
        output: {
          manualChunks: undefined,
          entryFileNames: 'assets/[name]-[hash].js',
          chunkFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash].[ext]'
        }
      }
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8003',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '/sp-api')
        }
      }
    },
    optimizeDeps: {
      include: ['element-plus']
    }
  }
})