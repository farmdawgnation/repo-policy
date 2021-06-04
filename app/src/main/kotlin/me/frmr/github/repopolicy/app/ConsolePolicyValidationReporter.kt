package me.frmr.github.repopolicy.app

import me.frmr.github.repopolicy.core.model.PolicyValidationResult

/**
 * Console reporter for policy validation
 */
object ConsolePolicyValidationReporter : PolicyValidationReporter {
  override fun report(results: List<PolicyValidationResult>): Int {
    var fails = 0
    results.groupBy { it.subject }.forEach { (subject, results) ->
      println("Results for $subject")

      results.forEach { result ->
        val prefix = if (result.passed) {
          "[ PASS ]"
        } else {
          fails++
          "[ FAIL ]"
        }

        println("$prefix ${result.description}")

        if (! result.extra.isNullOrBlank()) {
          println("         " + result.extra)
        }
      }

      println("")
    }

    return fails
  }
}
