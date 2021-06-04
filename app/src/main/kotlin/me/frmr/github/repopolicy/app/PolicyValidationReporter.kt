package me.frmr.github.repopolicy.app

import me.frmr.github.repopolicy.core.model.PolicyValidationResult

interface PolicyValidationReporter {
  fun report(results: List<PolicyValidationResult>): Int
}
