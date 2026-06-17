import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import fs from 'fs'

function createBuildVersion() {
    return new Date().toISOString().replace(/[-:.TZ]/g, '').slice(0, 14)
}

// 構建時生成 version.json（版本號唯一來源，與 Nginx $app_version 保持一致）
function generateVersionPlugin() {
    return {
        name: 'generate-version',
        buildStart() {
            const buildVersion = createBuildVersion()
            const publicDir = resolve(__dirname, 'public')
            if (!fs.existsSync(publicDir)) {
                fs.mkdirSync(publicDir, { recursive: true })
            }
            fs.writeFileSync(
                resolve(publicDir, 'version.json'),
                JSON.stringify({ version: buildVersion }, null, 2),
                'utf-8'
            )
            console.log(`[version] Generated version.json → ${buildVersion}`)
        }
    }
}

export default defineConfig(({ mode }) => {
    return {
        plugins: [vue(), generateVersionPlugin()],
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
    }
})
