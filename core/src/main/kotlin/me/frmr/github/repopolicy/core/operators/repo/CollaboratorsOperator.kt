package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

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

  override fun validate(target: GHRepository, github: GitHub): PolicyValidationResult {
    val associated = this.desiredCollaboratorsSet.groupBy { it.isTeam }
    val userCollaborators = associated[false]
    val teamCollaborators = associated[true]

    var validationPassed = true
    var validationErrored = false

    // Resolve teams
    if (teamCollaborators != null) {
      for (teamCollaborator in teamCollaborators) {
        val ghOrg = github.getOrganization(teamCollaborator.org)
        if (ghOrg == null) {
          validationErrored = true
          continue
        }

        val ghTeam = ghOrg.getTeamBySlug(teamCollaborator.name)
        if (ghTeam == null) {
          validationErrored = true
          continue
        }

        // This isn't the most efficient, but I can't get interacting with the
        // iterator directly to work for tests.
        val repoList = ghTeam.listRepositories().toList()
        val hasRepo = repoList.contains(target)

        if (! hasRepo) {
          validationPassed = false
        }
      }
    }

    // Resolve users
    if (userCollaborators != null) {
      for (userCollaborator in userCollaborators) {
        val ghUser = github.getUser(userCollaborator.name)
        if (ghUser == null) {
          validationErrored = true
          continue
        }

        // This isn't the most efficient, but I can't get interacting with the
        // iterator directly to work for tests.
        val repoList = ghUser.listRepositories().toList()
        val hasRepo = repoList.contains(target)

        if (! hasRepo) {
          validationPassed = false
        }
      }
    }

    var description = if (validationPassed) {
      "All collaborators are present."
    } else {
      "Some collaborators were missing."
    }

    if (validationErrored) {
      description += " Additionally, some teams or users could not be found."
    }

    return PolicyValidationResult(
      subject = target.fullName,
      description = description,
      passed = validationPassed
    )
  }
}
