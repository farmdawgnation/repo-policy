package me.frmr.github.repopolicy.core

import org.junit.jupiter.api.*

class PolicyEngineTest {
  @Test
  fun canDecodePolicy() {
    val examplePolicy = """
      ---
      # Some Policy metadata
      name: Example Policy
      version: 1.0
      author: Engineering Leaders <eng-leaders@example.org>
      # Rules associated with the policy
      rules:
      - org: ExampleOrg
        topic: example-topic
        # Rules match on a topic set on the repository and apply repo
        # and branch settings. You can omit the topic parameter or set
        # it to null to have this apply to all repositories owned by a
        # particular owner.
        exclude:
        - 'excluded-repo'
        # To exclude individual repositories from the policy, add the
        # name of the repository to this exclude list.
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
          - name: engineering-team
            org: MyOrg
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

    PolicyEngine(examplePolicy, false)
  }
}
