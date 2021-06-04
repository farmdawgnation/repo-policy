package me.frmr.github.repopolicy.app

import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult

/**
 * Console reporter for policy enforcement.
 */
object PolicyEnforcementReporter {
  fun report(results: List<PolicyEnforcementResult>): Int {
    var fails = 0
    results.groupBy { it.subject }.forEach { (subject, results) ->
      println("Results for $subject")

      results.forEach { result ->
        val prefix = if (result.passedValidation) {
          "[ PASS ]"
        } else if (result.policyEnforced) {
          "[ FIXD ]"
        } else {
          fails++
          "[ FAIL ]"
        }

        println("$prefix ${result.description}")
      }

      println("")
    }
    return fails
  }
}
