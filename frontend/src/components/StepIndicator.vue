<script setup lang="ts">
defineProps<{ current: number }>()
</script>

<template>
  <div class="indicator">
    <div
      v-for="n in 4"
      :key="n"
      class="dot"
      :class="{ active: n <= current }"
    />
  </div>
</template>
