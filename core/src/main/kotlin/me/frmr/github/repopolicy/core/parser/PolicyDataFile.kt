package me.frmr.github.repopolicy.core.parser

import kotlinx.serialization.*

@Serializable
data class PolicyRuleRequiredPullRequestReviewsDismissalRestrictions(
  val enabled: Boolean,
  val users: List<String>?,
  val teams: List<String>?
)

@Serializable
data class PolicyRuleRequiredPullRequestReviewsPushRestrictions(
  val enabled: Boolean,
  val users: List<String>?,
  val teams: List<String>?,
  val apps: List<String>?
)

@Serializable
data class PolicyRuleRequiredPullRequestReviews(
  val enabled: Boolean,
  val dismiss_stale_reviews: Boolean?,
  val require_code_owner_reviews: Boolean?,
  val required_approving_review_count: Int?,
  val dismissal_restrictions: PolicyRuleRequiredPullRequestReviewsDismissalRestrictions?,
  val push_restrictions: PolicyRuleRequiredPullRequestReviewsPushRestrictions?
)

@Serializable
data class PolicyRuleBranchRequiredStatusChecks(
  val enabled: Boolean,
  val contexts: List<String>,
  val strict: Boolean?,
  val enforce_admins: Boolean?
)

@Serializable
data class PolicyRuleBranchProtection(
  val enabled: Boolean,
  val required_linear_history: Boolean?,
  val allow_force_pushes: Boolean?,
  val allow_deletions: Boolean?,
  val required_pull_request_reviews: PolicyRuleRequiredPullRequestReviews?,
  val required_status_checks: PolicyRuleBranchRequiredStatusChecks?
)

@Serializable
data class PolicyRuleBranch(
  val branch: String,
  val protection: PolicyRuleBranchProtection?
)

@Serializable
data class PolicyRuleRepoFeatures(
  val issues: Boolean? = null,
  val projects: Boolean? = null,
  val wiki: Boolean? = null
)

@Serializable
data class PolicyRuleRepo(
  val license_key: String? = null,
  val delete_branch_on_merge: Boolean? = null,
  val visibility: String? = null,
  val features: PolicyRuleRepoFeatures? = null,
  val default_branch: String? = null,
  val collaborators: List<String>? = null
)

@Serializable
data class PolicyRule(
  val user: String? = null,
  val org: String? = null,
  val topic: String? = null,
  val repo: PolicyRuleRepo? = null,
  val branches: List<PolicyRuleBranch>? = null
)

@Serializable
data class PolicyDataFile(
  val name: String,
  val version: String,
  val author: String,
  val rules: List<PolicyRule>
)
