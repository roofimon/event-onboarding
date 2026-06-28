<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'
import { verifyToken, errorMessage } from '../services/api'

const router = useRouter()
const token = ref('')
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await verifyToken(store.applicationId as string, token.value)
    router.push({ name: 'fulfillment' })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <h2>Step 2 · Verify token</h2>
  <p class="hint">
    A 6-digit verification token was printed to the <strong>server console</strong>.
    Copy it here.
  </p>
  <form @submit.prevent="submit">
    <label for="token">Verification token</label>
    <input id="token" v-model="token" inputmode="numeric" required placeholder="000000" />
    <p v-if="error" class="error">{{ error }}</p>
    <button type="submit" :disabled="loading">
      {{ loading ? 'Verifying…' : 'Verify' }}
    </button>
  </form>
</template>
