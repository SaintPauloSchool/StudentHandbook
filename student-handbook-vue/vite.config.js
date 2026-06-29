import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => ({
    plugins: [vue()],
    base: '/',
    resolve: {
        alias: {
            '@': resolve(__dirname, 'src')
        }
    },
    build: {
        sourcemap: false,
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
    },
    esbuild: {
        drop: mode === 'production' ? ['console', 'debugger'] : []
    }
}))
