package me.frmr.github.repopolicy.core.model

/**
 * A *policy description* is a collection of policy metadata, policy subject
 * matchers that indicate what the subject applies to, and policy rules that
 * make up the ultimate policy.
 */
data class PolicyDescription(
  val name: String,
  val version: String,
  val author: String,
  val rules: List<PolicyRule>
)
