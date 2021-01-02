package me.frmr.github.repopolicy.core.model

import org.kohsuke.github.GHRepository

/**
 * A *policy rule* defines a particular rule that gets enforced against the
 * policy subject.
 */
interface PolicyRuleOperator {
  val description: String

  fun validate(target: GHRepository): PolicyValidationResult

  fun enforce(target: GHRepository): PolicyEnforcementResult
}
