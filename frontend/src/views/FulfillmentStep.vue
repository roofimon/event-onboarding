<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'
import { fulfillment, score, errorMessage } from '../services/api'

const router = useRouter()
const name = ref(store.name)
const email = ref(store.email)
const phone = ref(store.phone)
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const id = store.applicationId as string
    await fulfillment(id, {
      name: name.value,
      email: email.value,
      phone: phone.value,
    })
    store.name = name.value
    store.email = email.value
    store.phone = phone.value

    // Step 4 runs immediately after fulfillment.
    const result = await score(id)
    store.score = result.score
    store.approved = result.approved
    router.push({ name: result.approved ? 'welcome' : 'declined' })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <h2>Step 3 · Your details</h2>
  <p class="hint">Tell us a bit more so we can complete your application.</p>
  <form @submit.prevent="submit">
    <label for="name">Full name</label>
    <input id="name" v-model="name" required placeholder="Ada Lovelace" />

    <label for="email">Email address</label>
    <input id="email" v-model="email" type="email" required placeholder="you@example.com" />

    <label for="phone">Phone number</label>
    <input id="phone" v-model="phone" required placeholder="+1 555 0100" />

    <p v-if="error" class="error">{{ error }}</p>
    <button type="submit" :disabled="loading">
      {{ loading ? 'Scoring…' : 'Submit & get result' }}
    </button>
  </form>
</template>
