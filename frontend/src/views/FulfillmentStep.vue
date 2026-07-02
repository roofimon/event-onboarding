<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'
import { fulfillment, score, errorMessage } from '../services/api'

const router = useRouter()
const name = ref(store.name)
const email = ref(store.email)
const phone = ref(store.phone)
const salary = ref<number | null>(store.salary)
const salaryInput = ref(formatSalary(store.salary))
const yearsOfExperience = ref<number | null>(store.yearsOfExperience)
const loading = ref(false)
const error = ref('')

function formatSalary(value: number | null): string {
  return value === null ? '' : value.toLocaleString('en-US')
}

function onSalaryInput(event: Event) {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '')
  salary.value = digits ? Number(digits) : null
  salaryInput.value = formatSalary(salary.value)
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const id = store.applicationId as string
    await fulfillment(id, {
      name: name.value,
      email: email.value,
      phone: phone.value,
      salary: salary.value ?? 0,
      yearsOfExperience: yearsOfExperience.value ?? 0,
    })
    store.name = name.value
    store.email = email.value
    store.phone = phone.value
    store.salary = salary.value
    store.yearsOfExperience = yearsOfExperience.value

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

    <label for="salary">Salary</label>
    <input
      id="salary"
      :value="salaryInput"
      type="text"
      inputmode="numeric"
      required
      placeholder="120,000"
      @input="onSalaryInput"
    />

    <label for="yearsOfExperience">Years of experience</label>
    <input
      id="yearsOfExperience"
      v-model.number="yearsOfExperience"
      type="number"
      min="0"
      step="1"
      required
      placeholder="7"
    />

    <p v-if="error" class="error">{{ error }}</p>
    <button type="submit" :disabled="loading">
      {{ loading ? 'Scoring…' : 'Submit & get result' }}
    </button>
  </form>
</template>
