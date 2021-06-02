package me.frmr.github.repopolicy.core.operators.branch

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository

class BranchProtectionOperator(
  val branch: String,
  val enabled: Boolean,
  val requiredChecks: List<String>?,
  val dismissStaleReviews: Boolean?,
  val includeAdmins: Boolean?,
  val requireBranchIsUpToDate: Boolean?,
  val requireCodeOwnerReviews: Boolean?,
  val requiredReviewCount: Int?,
  val restrictPushAccess: Boolean?,
  val restrictReviewDismissals: Boolean?,
  val pushTeams: List<String>?,
  val pushUsers: List<String>?,
  val reviewDismissalUsers: List<String>?
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
      return PolicyEnforcementResult(
        subject = target.fullName + "/" + branch,
        description = "Basic branch protection rules are in place",
        passedValidation = true,
        policyEnforced = false
      )
    }

    return PolicyEnforcementResult(
      subject = target.fullName + "/" + branch,
      description = "Enforced basic branch protection rules",
      passedValidation = false,
      policyEnforced = true
    )
  }
}
