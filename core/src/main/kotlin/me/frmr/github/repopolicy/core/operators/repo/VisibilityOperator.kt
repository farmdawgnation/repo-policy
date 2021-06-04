package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository

/**
 * Validates the visibility of the repository. Does not profile an enforce function so as to not
 * cause unexpected visibility changes during an enforce action.
 */
class VisibilityOperator(private val requiredVisibility: String): NonEnforcingOperator() {
  override val description: String = "Repository visibility"

  override fun validate(target: GHRepository): PolicyValidationResult {
    return if (requiredVisibility == "private" && !target.isPrivate) {
      PolicyValidationResult(target.fullName, "Repository was public when should be private", false, null)
    } else if (requiredVisibility == "public" && target.isPrivate) {
      PolicyValidationResult(target.fullName, "Repository was private when should be public", false, null)
    } else {
      PolicyValidationResult(target.fullName, "Repository Visibility OK", passed = true, null)
    }
  }
}
