package me.frmr.github.repopolicy.core.operators.branch

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository

class BranchProtectionReviewsOperator(
  val branch: String,
  val enabled: Boolean,
  val dismissStaleReviews: Boolean?,
  val requireCodeOwnerReviews: Boolean?,
  val requiredApprovingReviewCount: Int?,
  val dismissalRestrictionsEnabled: Boolean?,
  val dismissalRestrictionsUsers: List<String>?,
  val dismissalRestrictionsTeams: List<String>?,
  val pushRestrictionsEnabled: Boolean?,
  val pushRestrictionsUsers: List<String>?,
  val pushRestrictionsTeams: List<String>?,
  val pushRestrictionsApps: List<String>?
): PolicyRuleOperator {
  override val description: String = "Required pull request reviews"

  override fun validate(target: GHRepository): PolicyValidationResult {
    TODO("Not yet implemented")
  }

  override fun enforce(target: GHRepository): PolicyEnforcementResult {
    TODO("Not yet implemented")
  }
}
