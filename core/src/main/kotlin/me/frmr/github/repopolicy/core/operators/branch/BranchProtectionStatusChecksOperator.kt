package me.frmr.github.repopolicy.core.operators.branch

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository

class BranchProtectionStatusChecksOperator(
  val branch: String,
  val enabled: Boolean,
  val contexts: List<String>,
  val strict: Boolean?,
  val enforceAdmins: Boolean?
): PolicyRuleOperator {
  override val description: String = "Required status checks"

  override fun validate(target: GHRepository): PolicyValidationResult {
    TODO("Not yet implemented")
  }

  override fun enforce(target: GHRepository): PolicyEnforcementResult {
    TODO("Not yet implemented")
  }
}
