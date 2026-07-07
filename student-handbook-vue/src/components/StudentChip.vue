<template>
  <div class="student-chip" :class="chipClass">
    <span class="student-chip__avatar" :style="avatarWrapStyle">
      <StudentPhoto :profile-number="profileNumber" :size="avatarSize" round />
    </span>
    <span class="student-chip__text">
      <span v-if="classSection" class="student-chip__class">{{ classSection }}</span>
      <span class="student-chip__name">{{ name }}</span>
    </span>
  </div>
</template>

<script>
import StudentPhoto from '@/components/StudentPhoto.vue'

export default {
  name: 'StudentChip',
  components: { StudentPhoto },
  props: {
    name: {
      type: String,
      default: ''
    },
    classSection: {
      type: String,
      default: ''
    },
    profileNumber: {
      type: [String, Number],
      default: ''
    },
    chipClass: {
      type: String,
      default: ''
    },
    avatarSize: {
      type: Number,
      default: 40
    }
  },
  computed: {
    avatarWrapStyle() {
      return {
        width: `${this.avatarSize}px`,
        height: `${this.avatarSize}px`
      }
    }
  }
}
</script>
