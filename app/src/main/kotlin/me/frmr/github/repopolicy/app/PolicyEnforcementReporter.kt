package me.frmr.github.repopolicy.app

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult

interface PolicyEnforcementReporter {
  fun report(results: List<PolicyEnforcementResult>): Int
}
