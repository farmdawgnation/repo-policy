package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * Policy rule operator that validates the default branch. Does not enforce so
 * as to avoid potentially super-disruptive branch moves as a part of an enforce
 * action.
 */
class DefaultBranchOperator(val defaultBranch: String): NonEnforcingOperator() {
  override val description: String = "Default branch name"

  override fun validate(target: GHRepository, github: GitHub): PolicyValidationResult {
    return if (target.defaultBranch == defaultBranch) {
      PolicyValidationResult(
        subject = target.fullName,
        description = "Default branch is $defaultBranch",
        passed = true
      )
    } else {
      PolicyValidationResult(
        subject = target.fullName,
        description = "Default branch is ${target.defaultBranch}, should be $defaultBranch",
        passed = false
      )
    }
  }
}
