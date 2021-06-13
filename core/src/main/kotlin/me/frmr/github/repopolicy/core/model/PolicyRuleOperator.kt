package me.frmr.github.repopolicy.core.model

import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * A *policy rule* defines a particular rule that gets enforced against the
 * policy subject.
 */
interface PolicyRuleOperator {
  val description: String

  /**
   * Validate the state of the repository against the policy
   */
  fun validate(target: GHRepository, github: GitHub): PolicyValidationResult

  /**
   * Enforce the policy against the current state of the repository
   */
  fun enforce(target: GHRepository, github: GitHub): PolicyEnforcementResult
}
