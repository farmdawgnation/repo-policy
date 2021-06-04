package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * Operator for validating and enforcing the use of the "delete branch on merge"
 * feature.
 */
class DeleteBranchOnMergeOperator(val enabled: Boolean): PolicyRuleOperator {
  override val description: String = "Delete branch on merge"

  override fun validate(target: GHRepository): PolicyValidationResult {
    return if (target.isDeleteBranchOnMerge == enabled) {
      PolicyValidationResult(
        subject = target.fullName,
        description = "Delete branch on merge matches policy",
        passed = true
      )
    } else {
      PolicyValidationResult(
        subject = target.fullName,
        description = "Delete branch ${target.isDeleteBranchOnMerge}, should be $enabled",
        passed = false
      )
    }
  }

  override fun enforce(target: GHRepository, github: GitHub): PolicyEnforcementResult {
    val validationResult = validate(target)

    if (validationResult.passed) {
      return PolicyEnforcementResult(
        subject = target.fullName,
        description = validationResult.description,
        passedValidation = true,
        policyEnforced = false
      )
    }

    target.deleteBranchOnMerge(enabled)

    return PolicyEnforcementResult(
      subject = target.fullName,
      description = "Delete branch on merge set to $enabled",
      passedValidation = false,
      policyEnforced = true
    )
  }
}
