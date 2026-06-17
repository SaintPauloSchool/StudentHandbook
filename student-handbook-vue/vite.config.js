import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import fs from 'fs'

function createBuildVersion() {
    return new Date().toISOString().replace(/[-:.TZ]/g, '').slice(0, 14)
}

// 構建時生成 version.json，並在 index.html 注入同步版本腳本（不依賴外部 JS，搶在 module 之前執行）
function generateVersionPlugin() {
    let buildVersion = ''

    return {
        name: 'generate-version',
        buildStart() {
            buildVersion = createBuildVersion()
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
        },
        transformIndexHtml: {
            order: 'post',
            handler(html) {
                if (!buildVersion) {
                    buildVersion = createBuildVersion()
                }
                const inlineScript = `<script>(function(){var v="${buildVersion}",f="_v",k="__app_cache_version",p=new URLSearchParams(location.search),c=p.get(f);if(c===v){try{localStorage.setItem(k,v)}catch(e){}return}try{localStorage.setItem(k,v)}catch(e){}p.set(f,v);location.replace(location.pathname+"?"+p+location.hash)})();</script>`
                return html.replace('</head>', `    ${inlineScript}\n  </head>`)
            }
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
