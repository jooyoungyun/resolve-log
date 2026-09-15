<template>
  <div class="page-heading">
    <div>
      <NuxtLink class="back-link" to="/goals">← 나의 결심</NuxtLink>
      <h1>새로운 결심</h1>
      <p>부담 없이 시작할 수 있는 작은 약속을 정해보세요.</p>
    </div>
    <span class="heading-number">01 / START</span>
  </div>
  <GoalForm @saved="navigateTo('/')" />
</template>
