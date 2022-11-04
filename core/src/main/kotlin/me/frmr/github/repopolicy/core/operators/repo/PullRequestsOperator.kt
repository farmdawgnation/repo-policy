package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * Operator for validating and enforcing the enabled/disabled status of Pull Requests settings on the repository
 */

class PullRequestsOperator(
  val allowMergeCommitEnabled: Boolean?,
  val allowSquashMergeEnabled: Boolean?,
  val allowRebaseMergeEnabled: Boolean?
) : PolicyRuleOperator {
  override val description: String = "Enforce enabled/disabled pull requests settings"

  private fun toggleLanguage(state: Boolean) = when (state) {
    false -> "disabled"
    true -> "enabled"
  }

  override fun validate(target: GHRepository, github: GitHub): PolicyValidationResult {
    var passedValidation = true
    val failureReasons: MutableList<String> = mutableListOf()

    if (allowMergeCommitEnabled != null && target.isAllowMergeCommit != allowMergeCommitEnabled) {
      passedValidation = false
      failureReasons.add(
        "allow merge commits is ${toggleLanguage(target.isAllowMergeCommit)}, should be ${
          toggleLanguage(
            allowMergeCommitEnabled
          )
        }"
      )
    }

    if (allowSquashMergeEnabled != null && target.isAllowSquashMerge != allowSquashMergeEnabled) {
      passedValidation = false
      failureReasons.add(
        "allow squash merging is ${toggleLanguage(target.isAllowSquashMerge)}, should be ${
          toggleLanguage(
            allowSquashMergeEnabled
          )
        }"
      )
    }

    if (allowRebaseMergeEnabled != null && target.isAllowRebaseMerge != allowRebaseMergeEnabled) {
      passedValidation = false
      failureReasons.add(
        "allow rebase merging is ${toggleLanguage(target.isAllowRebaseMerge)}, should be ${
          toggleLanguage(
            allowRebaseMergeEnabled
          )
        }"
      )
    }

    val fullDescription = failureReasons.joinToString(", ")

    return if (passedValidation) {
      PolicyValidationResult(
        subject = target.fullName,
        description = "Pull Request settings match policy",
        passed = true
      )
    } else {
      PolicyValidationResult(
        target.fullName,
        fullDescription,
        false
      )
    }
  }

  override fun enforce(target: GHRepository, github: GitHub): PolicyEnforcementResult {
    val validationResult = validate(target, github)

    if (validationResult.passed) {
      return PolicyEnforcementResult(
        subject = target.fullName,
        description = validationResult.description,
        passedValidation = true,
        policyEnforced = false
      )
    } else {
      if (allowSquashMergeEnabled != null &&
        allowRebaseMergeEnabled != null &&
        allowMergeCommitEnabled != null &&
        listOf(allowSquashMergeEnabled, allowRebaseMergeEnabled, allowMergeCommitEnabled).any { it }
      ) {
        // set all to true first to account for race condition
        target.allowMergeCommit(true)
        target.allowSquashMerge(true)
        target.allowRebaseMerge(true)

        target.allowMergeCommit(allowMergeCommitEnabled)
        target.allowSquashMerge(allowSquashMergeEnabled)
        target.allowRebaseMerge(allowRebaseMergeEnabled)

        return PolicyEnforcementResult(
          subject = target.fullName,
          description = "Pull Request settings updated",
          passedValidation = false,
          policyEnforced = true
        )
      } else {
        return PolicyEnforcementResult(
          subject = target.fullName,
          description = "Pull Request settings not updated. To update, set values for allow_merge_commit, allow_squash_merge, and allow_rebase_merge, and at least one must be enabled.",
          passedValidation = false,
          policyEnforced = false
        )
      }
    }
  }
}