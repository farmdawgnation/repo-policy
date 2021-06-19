package me.frmr.github.repopolicy.core

import com.charleskorn.kaml.Yaml
import me.frmr.github.repopolicy.core.model.*
import me.frmr.github.repopolicy.core.parser.PolicyDataFile
import me.frmr.github.repopolicy.core.parser.PolicyParser
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.PagedSearchIterable

/**
 * A *policy engine* consumes a *policy description* and validates that
 * reality matches the policy or makes changes to bring reality closer
 * to the policy.
 */
class PolicyEngine(val policy: PolicyDescription) {
  private lateinit var githubClient: GitHub

  constructor(dataFile: PolicyDataFile) : this(PolicyParser.parseDataFile(dataFile))

  constructor(yaml: String): this(Yaml.default.decodeFromString(PolicyDataFile.serializer(), yaml))

  /**
   * Init the GitHub Client from environment variables. Specifically:
   *
   * - GITHUB_LOGIN
   * - GITHUB_OAUTH
   * - GITHUB_ENDPOINT (for enterprise server installs)
   */
  fun initGithubClient() {
    this.githubClient = GitHubBuilder.fromEnvironment().build()
  }

  /**
   * Variation on initGithubClient that can inject a totally custom
   * github client.
   */
  fun initGithubClient(github: GitHub) {
    this.githubClient = github
  }

  private fun findMatchingRepos(subject: PolicySubjectMatchers): PagedSearchIterable<GHRepository>? {
    var searchRequest = githubClient.searchRepositories()

    var q = ""

    if (subject.topic != null) {
      q += "topic:${subject.topic} "
    }

    if (subject.user != null) {
      q += "user:${subject.user} "
    }

    if (subject.org != null) {
      q += "org:${subject.org} "
    }

    return searchRequest.q(q).list()
  }

  /**
   * Run validation over all rules in the policy.
   */
  fun validate(): List<PolicyValidationResult> {
    return policy.rules.flatMap { rule ->
      val repos = findMatchingRepos(rule.subject) ?: emptyList()
      repos.flatMap { repo ->
        // Determine if the repo is archived, if so, skip it
        if (repo.isArchived) {
          emptyList()
        } else {
          // Get the full repo — search results are abbreviated
          val fullRepo = githubClient.getRepository(repo.fullName)
          rule.operators.map { operator ->
            operator.validate(fullRepo, githubClient)
          }
        }
      }
    }
  }

  /**
   * Run enforcement over all rules in the policy.
   */
  fun enforce(): List<PolicyEnforcementResult> {
    return policy.rules.flatMap { rule ->
      val repos = findMatchingRepos(rule.subject) ?: emptyList()
      repos.flatMap { repo ->
        // Determine if the repo is archived, if so, skip it
        if (repo.isArchived) {
          emptyList()
        } else {
          rule.operators.map { operator ->
            operator.enforce(repo, this.githubClient)
          }
        }
      }
    }
  }
}
