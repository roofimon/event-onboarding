<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { store } from '../stores/onboarding'

const router = useRouter()
const profile = computed(() => store.profile)
const salaryDisplay = computed(() =>
  store.profile ? store.profile.salary.toLocaleString('en-US') : '',
)

function logout() {
  store.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div v-if="profile">
    <h2>Your profile</h2>
    <p class="hint">Signed in as {{ profile.email }}.</p>
    <dl class="profile">
      <div><dt>Name</dt><dd>{{ profile.name }}</dd></div>
      <div><dt>Email</dt><dd>{{ profile.email }}</dd></div>
      <div><dt>Phone</dt><dd>{{ profile.phone }}</dd></div>
      <div><dt>Salary</dt><dd>{{ salaryDisplay }}</dd></div>
      <div><dt>Years of experience</dt><dd>{{ profile.yearsOfExperience }}</dd></div>
    </dl>
    <button class="link-button" @click="logout">Log out</button>
  </div>
</template>
