package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository

class CollaboratorsOperator(desiredCollaborators: List<String>): NonEnforcingOperator() {
  override val description: String = "Collaborators"
  private val desiredCollaboratorsSet = desiredCollaborators.toSet()

  override fun validate(target: GHRepository): PolicyValidationResult {
    val currentCollaboratorsSet = target.collaboratorNames

    return if (currentCollaboratorsSet.containsAll(desiredCollaboratorsSet)) {
      PolicyValidationResult(
        subject = target.fullName,
        description = "All desired collaborators are present",
        passed = true
      )
    } else {
      val missingCollaborators = desiredCollaboratorsSet - currentCollaboratorsSet
      val descriptionStr = missingCollaborators.joinToString(", ")
      PolicyValidationResult(
        subject = target.fullName,
        description = "The following collaborators are missing: $descriptionStr",
        passed = false
      )
    }
  }
}
