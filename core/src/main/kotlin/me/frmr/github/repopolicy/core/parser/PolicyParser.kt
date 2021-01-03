package me.frmr.github.repopolicy.core.parser

import me.frmr.github.repopolicy.core.model.PolicyRule as ModelPolicyRule
import me.frmr.github.repopolicy.core.model.PolicyDescription
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicySubjectMatchers
import me.frmr.github.repopolicy.core.operators.repo.*

object PolicyParser {
  private fun createRepoOperators(input: PolicyRuleRepo?): List<PolicyRuleOperator> {
    val resultingOperators = mutableListOf<PolicyRuleOperator>()

    if (input?.license_key != null) {
      resultingOperators.add(LicenseOperator(input.license_key))
    }

    if (input?.delete_branch_on_merge != null) {
      resultingOperators.add(DeleteBranchOnMergeOperator(input.delete_branch_on_merge))
    }

    if (input?.visibility != null) {
      resultingOperators.add(VisibilityOperator(input.visibility))
    }

    if (input?.default_branch != null) {
      resultingOperators.add(DefaultBranchOperator(input.default_branch))
    }

    if (input?.features != null) {
      resultingOperators.add(FeaturesOperator(
        input.features.projects,
        input.features.issues,
        input.features.wiki
      ))
    }

    return resultingOperators
  }

  private fun parseRule(input: PolicyRule): ModelPolicyRule {
    val subjectMatchers = PolicySubjectMatchers(user = input.user, topic = input.topic, org = input.org)
    return ModelPolicyRule(
      subjectMatchers,
      createRepoOperators(input.repo)
    )
  }

  fun parseDataFile(input: PolicyDataFile): PolicyDescription {
    return PolicyDescription(
      name = input.name,
      version = input.version,
      author = input.author,
      rules = input.rules.map { parseRule(it) }
    )
  }
}
