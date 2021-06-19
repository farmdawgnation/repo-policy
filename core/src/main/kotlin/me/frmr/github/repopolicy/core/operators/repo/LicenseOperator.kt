package me.frmr.github.repopolicy.core.operators.repo

import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import me.frmr.github.repopolicy.core.operators.NonEnforcingOperator
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

/**
 * Operator that validates the license in use. Does not enforce.
 */
class LicenseOperator(val license: String): NonEnforcingOperator() {
  override val description: String = "License"

  override fun validate(target: GHRepository, github: GitHub): PolicyValidationResult {
    return if (target.license?.key == license) {
      PolicyValidationResult(
        subject = target.fullName,
        description = "License check matches",
        passed = true
      )
    } else {
      PolicyValidationResult(
        subject = target.fullName,
        description = "License was ${target.license?.key}, needed $license.",
        passed = false
      )
    }
  }
}
