package me.frmr.github.repopolicy.core.parser

import com.charleskorn.kaml.Yaml
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*

class PolicyDataFileTest {
  private val input = """
  ---
  # Some Policy metadata
  name: Example Policy
  version: 1.0
  author: Engineering Leaders <eng-leaders@example.org>
  # Rules associated with the policy
  rules:
  - owner: ExampleOrg
    topic: example-topic
    # Rules match on a topic set on the repository and apply repo
    # and branch settings. You can omit the topic parameter or set
    # it to null to have this apply to all repositories owned by a
    # particular owner.
    repo:
      license_key: mit
      delete_branch_on_merge: true
      visibility: public
      features:
        issues: true
        projects: false
        wiki: false
        vulnerability_alerts: true
      default_branch: main
      collaborators:
      - OrgName/engineering-team
    branches:
    - branch: main
      protection:
        enabled: true
        required_status_checks:
          enabled: true
          contexts:
          - "unit-tests"
          strict: false
          enforce_admins: false
        required_pull_request_reviews:
          enabled: true
          dismiss_stale_reviews: false
          require_code_owner_reviews: false
          required_approving_review_count: 1
          dissmissal_restrictions:
            enabled: true
            users:
            - user1
            teams:
            - team1
          push_restrictions:
            enabled: true
            users:
            - user1
            teams:
            - team1
            apps:
            - app1
        required_linear_history: false
        allow_force_pushes: false
        allow_deletions: false
  """.trimIndent()

  @Test
  fun canParseExampleInput() {
    val result = Yaml.default.decodeFromString(PolicyDataFile.serializer(), input)

    val expectedOutput = PolicyDataFile(
      name = "Example Policy",
      version = "1.0",
      author = "Engineering Leaders <eng-leaders@example.org>",
      rules = listOf(PolicyRule(
        owner = "ExampleOrg",
        topic = "example-topic",
        repo = PolicyRuleRepo(
          license_key = "mit",
          delete_branch_on_merge = true,
          visibility = "public",
          default_branch = "main",
          collaborators = listOf("OrgName/engineering-team"),
          features = PolicyRuleRepoFeatures(issues = true, projects = false, wiki = false, vulnerability_alerts = true)
        ),
        branches = listOf(PolicyRuleBranch(
          branch = "main",
          protection = PolicyRuleBranchProtection(
            enabled = true,
            required_linear_history = false,
            allow_force_pushes = false,
            allow_deletions = false,
            required_pull_request_reviews = PolicyRuleRequiredPullRequestReviews(
              enabled = true,
              dismiss_stale_reviews = false,
              require_code_owner_reviews = false,
              required_approving_review_count = 1,
              dissmissal_restrictions = PolicyRuleRequiredPullRequestReviewsDismissalRestrictions(
                enabled = true,
                users = listOf("user1"),
                teams = listOf("team1")
              ),
              push_restrictions = PolicyRuleRequiredPullRequestReviewsPushRestrictions(
                enabled = true,
                users = listOf("user1"),
                teams = listOf("team1"),
                apps = listOf("app1")
              )),
            required_status_checks = PolicyRuleBranchRequiredStatusChecks(
              enabled = true,
              contexts = listOf("unit-tests"),
              strict = false,
              enforce_admins = false
            )
          )
        ))
      ))
    )

    assertThat(result).isEqualTo(expectedOutput)
  }
}
