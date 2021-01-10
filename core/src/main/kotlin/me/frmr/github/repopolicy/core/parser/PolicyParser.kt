package me.frmr.github.repopolicy.core.parser

import me.frmr.github.repopolicy.core.model.PolicyRule as ModelPolicyRule
import me.frmr.github.repopolicy.core.model.PolicyDescription
import me.frmr.github.repopolicy.core.model.PolicyRuleOperator
import me.frmr.github.repopolicy.core.model.PolicySubjectMatchers
import me.frmr.github.repopolicy.core.operators.branch.BranchProtectionReviewsOperator
import me.frmr.github.repopolicy.core.operators.branch.BranchProtectionRootOperator
import me.frmr.github.repopolicy.core.operators.branch.BranchProtectionStatusChecksOperator
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
        input.features.issues,
        input.features.projects,
        input.features.wiki
      ))
    }

    if (input?.collaborators != null) {
      resultingOperators.add(CollaboratorsOperator(
        input.collaborators
      ))
    }

    return resultingOperators
  }

  private fun createBranchOperators(input: PolicyRuleBranch?): List<PolicyRuleOperator> {
    // Nothing to see here!
    if (input == null) {
      return emptyList()
    }

    // Branch isn't specified or isn't specified correctly
    if (input.branch == null || input.branch.trim() == "") {
      return emptyList()
    }

    // Also nothing to see here
    if (input.protection == null) {
      return emptyList()
    }

    val resultingOperators = mutableListOf<PolicyRuleOperator>()

    resultingOperators.add(BranchProtectionRootOperator(
      branch = input.branch,
      enabled = input.protection.enabled,
      requireLinearHistory = input.protection.required_linear_history,
      allowForcePushes = input.protection.allow_force_pushes,
      allowDeletions = input.protection.allow_deletions
    ))

    if (input.protection.enabled) {
      if (input.protection.required_status_checks != null) {
        resultingOperators.add(BranchProtectionStatusChecksOperator(
          branch = input.branch,
          enabled = input.protection.required_status_checks.enabled,
          contexts = input.protection.required_status_checks.contexts,
          strict = input.protection.required_status_checks.strict,
          enforceAdmins = input.protection.required_status_checks.enforce_admins
        ))
      }

      if (input.protection.required_pull_request_reviews != null) {
        resultingOperators.add(BranchProtectionReviewsOperator(
          branch = input.branch,
          enabled = input.protection.required_pull_request_reviews.enabled,
          dismissStaleReviews = input.protection.required_pull_request_reviews.dismiss_stale_reviews,
          requireCodeOwnerReviews = input.protection.required_pull_request_reviews.require_code_owner_reviews,
          requiredApprovingReviewCount = input.protection.required_pull_request_reviews.required_approving_review_count,
          dismissalRestrictionsEnabled = input.protection.required_pull_request_reviews.dismissal_restrictions?.enabled,
          dismissalRestrictionsUsers = input.protection.required_pull_request_reviews.dismissal_restrictions?.users,
          dismissalRestrictionsTeams = input.protection.required_pull_request_reviews.dismissal_restrictions?.teams,
          pushRestrictionsEnabled = input.protection.required_pull_request_reviews.push_restrictions?.enabled,
          pushRestrictionsUsers = input.protection.required_pull_request_reviews.push_restrictions?.users,
          pushRestrictionsTeams = input.protection.required_pull_request_reviews.push_restrictions?.teams,
          pushRestrictionsApps = input.protection.required_pull_request_reviews.push_restrictions?.apps
        ))
      }
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
