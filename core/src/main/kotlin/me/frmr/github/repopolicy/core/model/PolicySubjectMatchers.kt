package me.frmr.github.repopolicy.core.model

/**
 * Determines what subjects a policy will apply to.
 */
data class PolicySubjectMatchers(
  val user: String? = null,
  val org: String? = null,
  val topic: String? = null,
  val exclude: List<String>? =  null
)
