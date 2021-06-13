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
  - user: ExampleOrg
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
      default_branch: main
      collaborators:
      - org: OrgName
        name: engineering-team
        permission: admin
    branches:
    - branch: main
      protection:
        enabled: true
        required_checks:
        - "unit-tests"
        dismiss_stale_reviews: true
        include_admins: true
        require_up_to_date: true
        require_code_owner_reviews: true
        required_review_count: 2
        restrict_push_access: false
        restrict_review_dismissals: true
        push_teams:
        - "AnchorTab/engineering"
        push_users:
        - "farmdawgnation"
        review_dismissal_users:
        - "farmdawgnation"
  """.trimIndent()

  @Test
  fun canParseExampleInput() {
    val result = Yaml.default.decodeFromString(PolicyDataFile.serializer(), input)

    val expectedOutput = PolicyDataFile(
      name = "Example Policy",
      version = "1.0",
      author = "Engineering Leaders <eng-leaders@example.org>",
      rules = listOf(PolicyRule(
        user = "ExampleOrg",
        topic = "example-topic",
        repo = PolicyRuleRepo(
          license_key = "mit",
          delete_branch_on_merge = true,
          visibility = "public",
          default_branch = "main",
          collaborators = listOf(PolicyRuleRepoCollaborator("OrgName", "engineering-team", "admin")),
          features = PolicyRuleRepoFeatures(issues = true, projects = false, wiki = false)
        ),
        branches = listOf(PolicyRuleBranch(
          branch = "main",
          protection = PolicyRuleBranchProtection(
            enabled = true,
            required_checks = listOf("unit-tests"),
            dismiss_stale_reviews = true,
            include_admins = true,
            require_up_to_date = true,
            require_code_owner_reviews = true,
            required_review_count = 2,
            restrict_push_access = false,
            restrict_review_dismissals = true,
            push_teams = listOf("AnchorTab/engineering"),
            push_users = listOf("farmdawgnation"),
            review_dismissal_users = listOf("farmdawgnation")
        ))
      ))
    ))

    assertThat(result).isEqualTo(expectedOutput)
  }
}
