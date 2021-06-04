package me.frmr.github.repopolicy.core.model

/**
 * Representation of a single rule in the policy file
 */
data class PolicyRule(
  val subject: PolicySubjectMatchers,
  val operators: List<PolicyRuleOperator>
)
