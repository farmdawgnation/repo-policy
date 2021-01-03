package me.frmr.github.repopolicy.core.model

/**
 * The result of a policy validation.
 *
 * @param subject The subject the policy was run against.
 * @param description The human-description of the policy.
 * @param passed Whether or not the validation passed.
 * @param extra Any extra information about the validation.
 */
data class PolicyValidationResult(
  val subject: String,
  val description: String,
  val passed: Boolean,
  val extra: String? = null
)
