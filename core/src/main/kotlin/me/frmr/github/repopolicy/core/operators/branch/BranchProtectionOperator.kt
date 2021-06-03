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
    val ghBranch = target.getBranch(branch)
      ?: return PolicyValidationResult(
        subject = target.fullName + "/" + branch,
        description = "Branch does not exist",
        passed = false
      )

    val isProtected = ghBranch.isProtected
    if (isProtected != enabled) {
      val message = if (enabled) {
        "Branch protection not enabled"
      } else {
        "Branch protection enabled, should be disabled"
      }
      return PolicyValidationResult(
        subject = target.fullName + "/" + branch,
        description = message,
        passed = false
      )
    }

    if (isProtected) {
      val protectionDetails = ghBranch.protection
      val problems = mutableListOf<String>()

      if (requiredChecks != null && (protectionDetails.requiredStatusChecks == null || ! protectionDetails.requiredStatusChecks.contexts.containsAll(requiredChecks))) {
        problems.add("Missing required checks")
      }

      if (dismissStaleReviews != null && protectionDetails.requiredReviews?.isDismissStaleReviews != dismissStaleReviews) {
        if (dismissStaleReviews) {
          problems.add("Dismiss stale reviews disabled")
        } else {
          problems.add("Dismiss stale reviews enabled")
        }
      }

      if (includeAdmins != null && protectionDetails.enforceAdmins.isEnabled != includeAdmins) {
        if (includeAdmins) {
          problems.add("Include admins disabled")
        } else {
          problems.add("Include admins enabled")
        }
      }

      if (requireBranchIsUpToDate != null && protectionDetails.requiredStatusChecks?.isRequiresBranchUpToDate != requireBranchIsUpToDate) {
        if (requireBranchIsUpToDate) {
          problems.add("Require branch up to date is disabled")
        } else {
          problems.add("Require branch up to date is enabled")
        }
      }

      if (requireCodeOwnerReviews != null && protectionDetails.requiredReviews?.isRequireCodeOwnerReviews != requireCodeOwnerReviews) {
        if (requireCodeOwnerReviews) {
          problems.add("Code owner reviews are not required")
        } else {
          problems.add("Code owner reviews are required")
        }
      }

      if (requiredReviewCount != null && protectionDetails.requiredReviews?.requiredReviewers != requiredReviewCount) {
        val currentRequiredReviewers = protectionDetails.requiredReviews?.requiredReviewers
        problems.add("Requires $currentRequiredReviewers reviews, should require $requiredReviewCount")
      }

      if (restrictReviewDismissals != null) {
        if (restrictReviewDismissals && protectionDetails.requiredReviews?.dismissalRestrictions == null) {
          problems.add("Review dismissal restrictions missing")
        }

        if (!restrictReviewDismissals && protectionDetails.requiredReviews?.dismissalRestrictions != null) {
          problems.add("Review dismissal restrictions present")
        }

        if (protectionDetails.requiredReviews?.dismissalRestrictions != null) {
          if (reviewDismissalUsers != null && !protectionDetails.requiredReviews.dismissalRestrictions.users.map { it.name }.containsAll(
              reviewDismissalUsers
            )
          ) {
            problems.add("Missing users from review dismissal users")
          }
        }
      }

      if (restrictPushAccess != null) {
        if (restrictPushAccess == true && protectionDetails.restrictions == null) {
          problems.add("No push restrictions found")
        }

        if (restrictPushAccess == false && protectionDetails.restrictions != null) {
          problems.add("Push restrictions found")
        }

        if (protectionDetails.restrictions != null) {
          if (pushTeams != null && !protectionDetails.restrictions.teams.map{ it.name }.containsAll(pushTeams)) {
            problems.add("Teams missing from list of teams that can push")
          }

          if (pushUsers != null && !protectionDetails.restrictions.users.map{ it.name }.containsAll(pushUsers)) {
            problems.add("Users missing from list of users that can push")
          }
        }
      }

      if (problems.isNotEmpty()) {
        return PolicyValidationResult(
          subject = target.fullName + "/" + branch,
          description = "Branch protection rules do not match policy",
          passed = false,
          extra = problems.joinToString("; ")
        )
      }
    }

    return PolicyValidationResult(
      subject = target.fullName + "/" + branch,
      description = "Branch protection matches policy",
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
