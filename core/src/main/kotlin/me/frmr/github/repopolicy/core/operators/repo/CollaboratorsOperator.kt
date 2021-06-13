package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository

/**
 * Policy rule operator that validates that all collaborators requested are present
 * on the repository. This does not have an enforce mode.
 */
class CollaboratorsOperator(desiredCollaborators: List<CollaboratorsDetail>): NonEnforcingOperator() {
  companion object {
    /** Holder for details on collaborators **/
    data class CollaboratorsDetail(val org: String?, val name: String, val permission: String) {
      /** Determine if the collaborator reference is a team **/
      val isTeam = ! org.isNullOrEmpty()
    }
  }

  override val description: String = "Collaborators"
  private val desiredCollaboratorsSet = desiredCollaborators.toSet()

  override fun validate(target: GHRepository): PolicyValidationResult {
    val associated = this.desiredCollaboratorsSet.groupBy { it.isTeam }
    val userCollaborators = associated[false]
    val teamCollaborators = associated[true]

    // Resolve teams
    if (teamCollaborators != null) {
      for (teamCollaborator in teamCollaborators) {
      }
    }

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
