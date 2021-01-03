package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository

class CollaboratorsOperator: NonEnforcingOperator() {
  override val description: String = "Collaborators"

  override fun validate(target: GHRepository): PolicyValidationResult {
    TODO("Not yet implemented")
  }
}
