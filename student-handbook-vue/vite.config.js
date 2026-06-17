import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import fs from 'fs'

function createBuildVersion() {
    return new Date().toISOString().replace(/[-:.TZ]/g, '').slice(0, 14)
}

function generateVersionPlugin(version) {
    return {
        name: 'generate-version',
        buildStart() {
            const publicDir = resolve(__dirname, 'public')
            if (!fs.existsSync(publicDir)) {
                fs.mkdirSync(publicDir, { recursive: true })
            }
            fs.writeFileSync(
                resolve(publicDir, 'version.json'),
                JSON.stringify({ version }, null, 2),
                'utf-8'
            )
            console.log(`[version] base=/${version}/  version.json → ${version}`)
        }
    }
}

export default defineConfig(({ command, mode }) => {
    const buildVersion = createBuildVersion()
    const useVersionedBase = command === 'build'
    const base = useVersionedBase ? `/${buildVersion}/` : '/'

    return {
        plugins: [vue(), generateVersionPlugin(buildVersion)],
        base,
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
