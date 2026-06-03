import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
    // 前後端分離後，前端服務在Nginx直接使用根路徑作爲基礎路徑
    const base = '/';

    return {
        plugins: [vue()],
        // 根據環境設置基礎路徑
        base: base,
        resolve: {
            alias: {
                '@': resolve(__dirname, 'src')
            }
        },
        build: {
            // 關閉 sourcemap，防止源碼泄漏
            sourcemap: false,
            // 確保資源路徑正確
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
            // 生產環境下移除 console.log 和 debugger
            drop: mode === 'production' ? ['console', 'debugger'] : []
        }
    }
})