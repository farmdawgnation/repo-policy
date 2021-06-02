package me.frmr.github.repopolicy.core.parser

import kotlinx.serialization.*

@Serializable
data class PolicyRuleBranchProtection(
  val enabled: Boolean,
  val required_checks: List<String>? = null,
  val dismiss_stale_reviews: Boolean? = null,
  val include_admins: Boolean? = null,
  val require_up_to_date: Boolean? = null,
  val require_code_owner_reviews: Boolean? = null,
  val required_review_count: Int? = null,
  val restrict_push_access: Boolean? = null,
  val restrict_review_dismissals: Boolean? = null,
  val push_teams: List<String>? = null,
  val push_users: List<String>? = null,
  val review_dismissal_users: List<String>? = null
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
