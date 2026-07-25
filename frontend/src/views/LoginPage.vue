<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'
import { login, errorMessage } from '../services/api'

const router = useRouter()
const email = ref(store.email)
const password = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    store.profile = await login(email.value, password.value)
    await router.push({name: 'profile'})
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <h2>Log in</h2>
  <p class="hint">Use the credentials sent to you when your application was approved.</p>
  <form @submit.prevent="submit">
    <label for="login-email">Email address</label>
    <input id="login-email" v-model="email" type="email" required placeholder="you@example.com" />
    <label for="login-password">Password</label>
    <input id="login-password" v-model="password" type="password" required autocomplete="current-password" />
    <p v-if="error" class="error">{{ error }}</p>
    <button type="submit" :disabled="loading">
      {{ loading ? 'Signing in…' : 'Log in' }}
    </button>
  </form>
</template>
