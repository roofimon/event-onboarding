<script setup lang="ts">
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import StepIndicator from './components/StepIndicator.vue'

const route = useRoute()
// Map routes to the 1-4 step number; outcome pages show no indicator.
const stepByName: Record<string, number> = { email: 1, verify: 2, fulfillment: 3 }
const currentStep = computed<number | null>(() => stepByName[route.name as string] ?? null)
</script>

<template>
  <div class="page">
    <header class="header">
      <h1>Event Onboarding</h1>
    </header>
    <main class="card">
      <StepIndicator v-if="currentStep" :current="currentStep" />
      <router-view />
    </main>
  </div>
</template>
