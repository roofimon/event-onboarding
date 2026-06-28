<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'
import { start, errorMessage } from '../services/api'

const router = useRouter()
const email = ref(store.email)
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const res = await start(email.value)
    store.reset()
    store.applicationId = res.applicationId
    store.email = email.value
    router.push({ name: 'verify' })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <h2>Step 1 · Your email</h2>
  <p class="hint">Enter your email to begin. We'll send you a verification token.</p>
  <form @submit.prevent="submit">
    <label for="email">Email address</label>
    <input id="email" v-model="email" type="email" required placeholder="you@example.com" />
    <p v-if="error" class="error">{{ error }}</p>
    <button type="submit" :disabled="loading">
      {{ loading ? 'Sending…' : 'Continue' }}
    </button>
  </form>
</template>
