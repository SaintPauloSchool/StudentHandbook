import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
    // 前后端分离后，前端服务在Nginx直接使用根路径作为基础路径
    const base = '/';

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
                '/sp-api': {
                    target: 'http://localhost:8002',
                    changeOrigin: false
                }
            }
        },
        optimizeDeps: {
            include: ['element-plus']
        }
    }
})