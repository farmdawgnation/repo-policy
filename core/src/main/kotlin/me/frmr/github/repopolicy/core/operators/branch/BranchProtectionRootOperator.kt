package me.frmr.github.repopolicy.core.operators.branch

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository

class BranchProtectionRootOperator: PolicyRuleOperator {
  override val description: String = "Branch protection"

  override fun validate(target: GHRepository): PolicyValidationResult {
    TODO("Not yet implemented")
  }

  override fun enforce(target: GHRepository): PolicyEnforcementResult {
    TODO("Not yet implemented")
  }
}