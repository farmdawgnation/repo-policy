package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository

class LicenseOperator(val license: String): NonEnforcingOperator() {
  override val description: String = "License"

  override fun validate(target: GHRepository): PolicyValidationResult {
    return if (target.license.key == license) {
      PolicyValidationResult(
        subject = target.fullName,
        description = "License check matches",
        passed = true
      )
    } else {
      PolicyValidationResult(
        subject = target.fullName,
        description = "License was ${target.license}, needed $license.",
        passed = false
      )
    }
  }
}
