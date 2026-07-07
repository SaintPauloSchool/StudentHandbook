<template>
  <div class="student-photo" :class="{ 'student-photo--round': round }" :style="wrapperStyle">
    <img
      v-if="photoSrc"
      :src="photoSrc"
      alt=""
      class="student-photo__image"
      :style="imageStyle"
    />
    <div v-else-if="loading" class="student-photo__placeholder student-photo__loading" :style="placeholderStyle">
      <el-icon class="is-loading" :style="iconStyle"><Loading /></el-icon>
    </div>
    <div v-else class="student-photo__placeholder" :style="placeholderStyle">
      <el-icon :style="iconStyle"><User /></el-icon>
    </div>
  </div>
</template>

<script>
import { User, Loading } from '@element-plus/icons-vue'
import service from '@/utils/request.js'
import { API_ENDPOINTS } from '@/config/api.js'

const photoCache = new Map()

function acquireCachedPhoto(profileNumber) {
  const entry = photoCache.get(profileNumber)
  if (!entry) return null
  entry.refs += 1
  return entry.url
}

function putCachedPhoto(profileNumber, url) {
  photoCache.set(profileNumber, { url, refs: 1 })
}

function releaseCachedPhoto(profileNumber) {
  if (!profileNumber) return
  const entry = photoCache.get(profileNumber)
  if (!entry) return
  entry.refs -= 1
  if (entry.refs <= 0) {
    URL.revokeObjectURL(entry.url)
    photoCache.delete(profileNumber)
  }
}

export default {
  name: 'StudentPhoto',
  components: { User, Loading },
  props: {
    profileNumber: {
      type: [String, Number],
      default: ''
    },
    size: {
      type: Number,
      default: 40
    },
    round: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      photoSrc: '',
      loading: false,
      cacheKey: ''
    }
  },
  computed: {
    normalizedProfileNumber() {
      if (this.profileNumber === null || this.profileNumber === undefined || this.profileNumber === '') {
        return ''
      }
      return String(this.profileNumber).trim()
    },
    wrapperStyle() {
      return {
        width: `${this.size}px`,
        height: `${this.size}px`
      }
    },
    imageStyle() {
      const radius = this.round ? '50%' : (this.size >= 80 ? '8px' : '4px')
      return {
        width: `${this.size}px`,
        height: `${this.size}px`,
        borderRadius: radius
      }
    },
    placeholderStyle() {
      return this.imageStyle
    },
    iconStyle() {
      const fontSize = Math.max(14, Math.round(this.size * 0.45))
      return { fontSize: `${fontSize}px` }
    }
  },
  watch: {
    normalizedProfileNumber: {
      immediate: true,
      handler() {
        this.loadPhoto()
      }
    }
  },
  beforeUnmount() {
    this.releaseCurrentPhoto()
  },
  methods: {
    releaseCurrentPhoto() {
      if (this.cacheKey) {
        releaseCachedPhoto(this.cacheKey)
        this.cacheKey = ''
      }
      this.photoSrc = ''
    },
    async loadPhoto() {
      this.releaseCurrentPhoto()
      this.loading = false

      const profileNumber = this.normalizedProfileNumber
      if (!/^[0-9]{1,20}$/.test(profileNumber)) {
        return
      }

      const cachedUrl = acquireCachedPhoto(profileNumber)
      if (cachedUrl) {
        this.cacheKey = profileNumber
        this.photoSrc = cachedUrl
        return
      }

      this.loading = true
      try {
        const response = await service.get(`${API_ENDPOINTS.STUDENT_PHOTO}/${profileNumber}`, {
          responseType: 'blob'
        })
        const blob = response.data
        if (blob && blob.size > 0 && blob.type && blob.type.startsWith('image/')) {
          if (this.normalizedProfileNumber !== profileNumber) {
            return
          }
          const objectUrl = URL.createObjectURL(blob)
          putCachedPhoto(profileNumber, objectUrl)
          this.cacheKey = profileNumber
          this.photoSrc = objectUrl
        }
      } catch (e) {
        // 無照片時顯示佔位
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.student-photo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.student-photo__image {
  display: block;
  object-fit: cover;
  background: #fff;
}

.student-photo--round .student-photo__image,
.student-photo--round .student-photo__placeholder {
  border: none;
}

.student-photo__placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #ffffff;
}

.student-photo__loading {
  background: #e2e8f0;
  color: #94a3b8;
}
</style>
