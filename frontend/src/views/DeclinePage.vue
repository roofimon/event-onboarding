<script setup lang="ts">
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'

const router = useRouter()

function restart() {
  store.reset()
  router.push({ name: 'email' })
}
</script>

<template>
  <div class="outcome declined">
    <h2>Application not approved</h2>
    <p class="hint">
      Unfortunately your credit score did not meet the threshold this time.
    </p>
    <div class="score">{{ store.score }}</div>
    <p class="hint">Credit score (approval requires &gt; 40)</p>
    <button class="link-button" @click="restart">Try again</button>
  </div>
</template>
