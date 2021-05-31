package me.frmr.github.repopolicy.core.operators.branch

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository

class BranchProtectionRootOperator(
  val branch: String,
  val enabled: Boolean,
  val requireLinearHistory: Boolean?, // TODO: listen to this value
  val allowForcePushes: Boolean?, // TODO: listen to this value
  val allowDeletions: Boolean? // TODO: listen to this value
): PolicyRuleOperator {
  override val description: String = "Branch protection"

  override fun validate(target: GHRepository): PolicyValidationResult {
    val branch = target.getBranch(branch)

    if (branch == null) {
      return PolicyValidationResult(
        subject = target.fullName + "/" + branch,
        description = "Branch does not exist",
        passed = false
      )
    }

    val isProtected = branch.isProtected
    if (isProtected != enabled) {
      return PolicyValidationResult(
        subject = target.fullName + "/" + branch,
        description = "Expected protection $enabled found protection $isProtected",
        passed = false
      )
    }

    return PolicyValidationResult(
      subject = target.fullName + "/" + branch,
      description = "Basic branch protection ok",
      passed = true
    )
  }

  override fun enforce(target: GHRepository): PolicyEnforcementResult {
    val validationResult = validate(target)

    if (validationResult.passed) {

    }

    return PolicyEnforcementResult(
      subject = target.fullName + "/" + branch,
      description = "Enforced basic branch protection rules",
      passedValidation = false,
      policyEnforced = true
    )
  }
}
