<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'
import { updateProfile, errorMessage } from '../services/api'

const router = useRouter()
const profile = computed(() => store.profile)
const salaryDisplay = computed(() =>
  store.profile ? store.profile.salary.toLocaleString('en-US') : '',
)

const editing = ref(false)
const saving = ref(false)
const error = ref('')
const form = reactive({
  name: '',
  phone: '',
  salary: 0,
  yearsOfExperience: 0,
  password: '',
})
// Masked, comma-formatted display for the salary field (mirrors the fulfillment step).
const salaryInput = ref('')

function formatSalary(value: number): string {
  return value ? value.toLocaleString('en-US') : ''
}

function onSalaryInput(event: Event) {
  const digits = (event.target as HTMLInputElement).value.replace(/\D/g, '')
  form.salary = digits ? Number(digits) : 0
  salaryInput.value = formatSalary(form.salary)
}

function startEdit() {
  if (!store.profile) return
  error.value = ''
  form.name = store.profile.name
  form.phone = store.profile.phone
  form.salary = store.profile.salary
  salaryInput.value = formatSalary(store.profile.salary)
  form.yearsOfExperience = store.profile.yearsOfExperience
  form.password = ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function save() {
  if (!store.profile) return
  error.value = ''
  saving.value = true
  try {
    store.profile = await updateProfile({
      email: store.profile.email,
      password: form.password,
      name: form.name,
      phone: form.phone,
      salary: Number(form.salary),
      yearsOfExperience: Number(form.yearsOfExperience),
    })
    editing.value = false
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    saving.value = false
  }
}

function logout() {
  store.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div v-if="profile">
    <h2>Your profile</h2>
    <p class="hint">Signed in as {{ profile.email }}.</p>

    <dl v-if="!editing" class="profile">
      <div><dt>Name</dt><dd>{{ profile.name }}</dd></div>
      <div><dt>Email</dt><dd>{{ profile.email }}</dd></div>
      <div><dt>Phone</dt><dd>{{ profile.phone }}</dd></div>
      <div><dt>Salary</dt><dd>{{ salaryDisplay }}</dd></div>
      <div><dt>Years of experience</dt><dd>{{ profile.yearsOfExperience }}</dd></div>
    </dl>

    <template v-if="!editing">
      <button type="button" @click="startEdit">Edit profile</button>
      <button class="link-button" @click="logout">Log out</button>
    </template>

    <form v-else @submit.prevent="save">
      <label for="edit-name">Full name</label>
      <input id="edit-name" v-model="form.name" type="text" required />
      <label for="edit-phone">Phone number</label>
      <input id="edit-phone" v-model="form.phone" type="tel" required />
      <label for="edit-salary">Salary</label>
      <input
        id="edit-salary"
        :value="salaryInput"
        type="text"
        inputmode="numeric"
        required
        placeholder="120,000"
        @input="onSalaryInput"
      />
      <label for="edit-years">Years of experience</label>
      <input id="edit-years" v-model.number="form.yearsOfExperience" type="number" min="0" required />
      <label for="edit-password">Confirm password</label>
      <input
        id="edit-password"
        v-model="form.password"
        type="password"
        required
        autocomplete="current-password"
      />
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="saving">{{ saving ? 'Saving…' : 'Save changes' }}</button>
      <button type="button" class="link-button" @click="cancelEdit">Cancel</button>
    </form>
  </div>
</template>
