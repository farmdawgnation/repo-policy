package me.frmr.github.repopolicy.core.model

/**
 * Determines what subjects a policy will apply to.
 */
data class PolicySubjectMatchers(
  val owner: String?,
  val topic: String?
)
