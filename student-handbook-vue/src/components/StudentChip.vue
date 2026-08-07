<template>
  <div class="student-chip" :class="chipClass">
    <span class="student-chip__text">
      <span v-if="classSection" class="student-chip__class">{{ classSection }}</span>
      <span class="student-chip__name">{{ name }}</span>
    </span>
  </div>
</template>

<script>
export default {
  name: 'StudentChip',
  props: {
    name: {
      type: String,
      default: ''
    },
    classSection: {
      type: String,
      default: ''
    },
    chipClass: {
      type: String,
      default: ''
    }
  }
}
</script>
