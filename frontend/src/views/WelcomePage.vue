<script setup lang="ts">
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'

const router = useRouter()

function restart() {
  store.reset()
  router.push({ name: 'email' })
}

function goToLogin() {
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="outcome approved">
    <h2>🎉 Welcome aboard{{ store.name ? ', ' + store.name : '' }}!</h2>
    <p class="hint">Your application has been approved.</p>
    <div class="score">{{ store.score }}</div>
    <p class="hint">Credit score (approval requires &gt; 40)</p>
    <button type="button" @click="goToLogin">Log in to your account</button>
    <button class="link-button" @click="restart">Start a new application</button>
  </div>
</template>
