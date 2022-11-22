package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GHOrganization
import org.kohsuke.github.GHTeam
import org.kohsuke.github.GHUser

/**
 * Policy rule operator that validates that all collaborators requested are present
 * on the repository. This does not have an enforce mode.
 */
class CollaboratorsOperator(desiredCollaborators: List<CollaboratorsDetail>) : PolicyRuleOperator {
    companion object {
        /** Holder for details on collaborators **/
        data class CollaboratorsDetail(val org: String?, val name: String, val permission: String) {
            /** Determine if the collaborator reference is a team **/
            val isTeam = !org.isNullOrEmpty()
        }
    }

    override val description: String = "Collaborators"
    private val desiredCollaboratorsSet = desiredCollaborators.toSet()
    private val associated = this.desiredCollaboratorsSet.groupBy { it.isTeam }
    private val userCollaborators = associated[false]
    private val teamCollaborators = associated[true]
    private val desiredTeamsMap: MutableMap<GHOrganization.Permission, MutableSet<GHTeam>> = mutableMapOf()
    private val desiredUsersMap: MutableMap<GHOrganization.Permission, MutableSet<GHUser>> = mutableMapOf()

    private fun mapGhPermissions(permission: String?): GHOrganization.Permission? {
        return when (permission) {
            "admin" -> GHOrganization.Permission.ADMIN
            "maintain" -> GHOrganization.Permission.MAINTAIN
            "write" -> GHOrganization.Permission.PUSH
            "triage" -> GHOrganization.Permission.TRIAGE
            "read" -> GHOrganization.Permission.PULL
            else -> null
        }
    }

    override fun validate(target: GHRepository, github: GitHub): PolicyValidationResult {
        var validationPassed = true
        var validationErrored = false

        // Resolve teams
        if (!teamCollaborators.isNullOrEmpty()) {
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

                if (!hasRepo) {
                    validationPassed = false
                }

                val permission = mapGhPermissions(teamCollaborator.permission)
                if (permission != null) {
                    desiredTeamsMap.getOrPut(permission) { mutableSetOf() }.add(ghTeam)
                }
            }
        }

        // Resolve users
        if (!userCollaborators.isNullOrEmpty()) {
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

                if (!hasRepo) {
                    validationPassed = false
                }

                val permission = mapGhPermissions(userCollaborator.permission)
                if (permission != null) {
                    desiredUsersMap.getOrPut(permission) { mutableSetOf() }.add(ghUser)
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

    override fun enforce(target: GHRepository, github: GitHub): PolicyEnforcementResult {
        val validationResult = validate(target, github)

        return if (validationResult.passed) {
            PolicyEnforcementResult(
                    subject = target.fullName,
                    description = validationResult.description,
                    passedValidation = true,
                    policyEnforced = false
            )
        } else {
            target.removeCollaborators(target.collaborators)
            target.teams.forEach { it.remove(target) }

            desiredUsersMap.forEach { (permission, users) ->
                users.forEach { user ->
                    target.addCollaborators(permission, user)
                }
            }

            desiredTeamsMap.forEach { (permission, teams) ->
                teams.forEach { team ->
                    team.add(target, permission)
                }
            }

            PolicyEnforcementResult(
                    subject = target.fullName,
                    description = "Collaborators updated",
                    passedValidation = false,
                    policyEnforced = true
            )
        }
    }
}
