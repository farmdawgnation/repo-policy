package me.frmr.github.repopolicy.core.parser

import me.frmr.github.repopolicy.core.model.PolicyRule as ModelPolicyRule
import me.frmr.github.repopolicy.core.model.PolicyDescription
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicySubjectMatchers
import me.frmr.github.repopolicy.core.operators.branch.BranchProtectionOperator
import me.frmr.github.repopolicy.core.operators.repo.*

/**
 * Parses the YAML like data structure into PolicyRuleOperator instances for running from
 * PolicyEngine
 */
object PolicyParser {
  private fun createRepoOperators(input: PolicyRuleRepo?): MutableList<PolicyRuleOperator> {
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
        input.features.issues,
        input.features.projects,
        input.features.wiki
      ))
    }

    if (input?.collaborators != null) {
      resultingOperators.add(CollaboratorsOperator(
        input.collaborators.map { pdfc ->
          CollaboratorsOperator.Companion.CollaboratorsDetail(pdfc.org, pdfc.name, pdfc.permission)
        }
      ))
    }

    return resultingOperators
  }

  private fun createBranchOperators(input: PolicyRuleBranch?): MutableList<PolicyRuleOperator> {
    // Nothing to see here!
    if (input == null) {
      return mutableListOf()
    }

    // Branch isn't specified or isn't specified correctly
    if (input.branch.trim() == "") {
      return mutableListOf()
    }

    // Also nothing to see here
    if (input.protection == null) {
      return mutableListOf()
    }

    val resultingOperators = mutableListOf<PolicyRuleOperator>()

    resultingOperators.add(BranchProtectionOperator(
      branch = input.branch,
      enabled = input.protection.enabled,
      requiredChecks = input.protection.required_checks,
      dismissStaleReviews = input.protection.dismiss_stale_reviews,
      includeAdmins = input.protection.include_admins,
      requireBranchIsUpToDate = input.protection.require_up_to_date,
      requireCodeOwnerReviews = input.protection.require_code_owner_reviews,
      requiredReviewCount = input.protection.required_review_count,
      restrictPushAccess = input.protection.restrict_push_access,
      restrictReviewDismissals = input.protection.restrict_review_dismissals,
      pushTeams = input.protection.push_teams,
      pushUsers = input.protection.push_users,
      reviewDismissalUsers = input.protection.review_dismissal_users
    ))

    return resultingOperators
  }

  private fun parseRule(input: PolicyRule): ModelPolicyRule {
    val subjectMatchers = PolicySubjectMatchers(user = input.user, topic = input.topic, org = input.org)
    val branchOperators: List<PolicyRuleOperator> = if (input.branches != null) {
      input.branches.flatMap { createBranchOperators(it) }
    } else {
      emptyList()
    }
    val allOperators: MutableList<PolicyRuleOperator> = createRepoOperators(input.repo)
    allOperators.addAll(branchOperators)
    return ModelPolicyRule(
      subjectMatchers,
      allOperators,
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
