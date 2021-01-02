package me.frmr.github.repopolicy.core.model

data class PolicyRule(
  val subject: PolicySubjectMatchers,
  val operators: List<PolicyRuleOperator>
)
