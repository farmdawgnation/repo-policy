package me.frmr.github.repopolicy.core.model

/**
 * The result of a policy enforcement.
 *
 * @param subject The subject the policy was run against.
 * @param description The human-description of the policy.
 * @param passedValidation Whether or not the policy validated.
 * @param policyEnforced
 */
data class PolicyEnforcementResult(
  val subject: String,
  val description: String,
  val passedValidation: Boolean,
  val policyEnforced: Boolean,
  val extra: String? = null
)
