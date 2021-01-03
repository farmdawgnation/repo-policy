package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.kohsuke.github.GHRepository

class VisibilityOperator(private val requiredVisibility: String): PolicyRuleOperator {
  override val description: String = "Repository visibility"

  override fun validate(target: GHRepository): PolicyValidationResult {
    return if (requiredVisibility == "private" && !target.isPrivate) {
      PolicyValidationResult(target.fullName, "Repository was public when should be private", false, null)
    } else if (requiredVisibility == "public" && target.isPrivate) {
      PolicyValidationResult(target.fullName, "Repository was private when should be public", false, null)
    } else {
      PolicyValidationResult(target.fullName, "OK", passed = true, null)
    }
  }

  override fun enforce(target: GHRepository): PolicyEnforcementResult {
    val validationResult = validate(target)

    return if (validationResult.passed) {
      PolicyEnforcementResult(target.fullName, "Nothing to do",true, false, null)
    } else {
      val updatedDescription = validationResult.description + " – This rule will not change visibility to correct this issue"
      PolicyEnforcementResult(target.fullName, updatedDescription,false, false, null)
    }
  }
}
