package me.frmr.github.repopolicy.core.operators

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * Some operators don't include the ability to automatically enforce their
 * rule. This may be due to the complexity or something else.
 */
abstract class NonEnforcingOperator: PolicyRuleOperator {
  override fun enforce(target: GHRepository, github: GitHub): PolicyEnforcementResult {
    val validationResult = validate(target, github)

    return if (validationResult.passed) {
      PolicyEnforcementResult(target.fullName, "Nothing to do",true, false, null)
    } else {
      val updatedDescription = validationResult.description + " – This rule will not change visibility to correct this issue"
      PolicyEnforcementResult(target.fullName, updatedDescription,false, false, null)
    }
  }
}
