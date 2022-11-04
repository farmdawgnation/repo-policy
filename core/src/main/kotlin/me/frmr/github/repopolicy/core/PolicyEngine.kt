package me.frmr.github.repopolicy.core

import com.charleskorn.kaml.Yaml
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import me.frmr.github.repopolicy.core.model.*
import me.frmr.github.repopolicy.core.parser.PolicyDataFile
import me.frmr.github.repopolicy.core.parser.PolicyParser
import org.kohsuke.github.*
import java.security.Key
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import java.util.Objects


/**
 * A *policy engine* consumes a *policy description* and validates that
 * reality matches the policy or makes changes to bring reality closer
 * to the policy.
 */
class PolicyEngine(val policy: PolicyDescription, val logging: Boolean) {
  private lateinit var githubClient: GitHub
  private var appInstallationTokenTtl: Long = 0

  constructor(dataFile: PolicyDataFile, logging: Boolean) : this(PolicyParser.parseDataFile(dataFile), logging)

  constructor(yaml: String, logging: Boolean) : this(
    Yaml.default.decodeFromString(PolicyDataFile.serializer(), yaml),
    logging
  )

  /**
   * Init the GitHub Client from environment variables. Specifically:
   *
   * - GITHUB_OAUTH
   * - GITHUB_APP_KEY
   * - GITHUB_APP_ID
   * - GITHUB_APP_INSTALLATION_ORG
   * - GITHUB_ENDPOINT (for enterprise server installs)
   */
  fun initGithubClient() {
    if (System.getenv("GITHUB_OAUTH") != null) {
      this.githubClient = GitHubBuilder.fromEnvironment().build()
    } else if (System.getenv("GITHUB_APP_KEY") != null && System.getenv("GITHUB_APP_ID") != null) {
      // Authenticating as a GitHub App via JWT token
      // https://github-api.kohsuke.org/githubappjwtauth.html
      val jwtToken: String = this.createJWT()
      val jwtGithubClient = GitHubBuilder().withJwtToken(jwtToken).build()

      // Authenticating as an installation via App Installation Token
      // https://github-api.kohsuke.org/githubappappinsttokenauth.html
      val appInstallation: GHAppInstallation = jwtGithubClient.app
        .getInstallationByOrganization(System.getenv("GITHUB_APP_INSTALLATION_ORG")) // Installation Id
      val appInstallationToken = appInstallation.createToken().create().token

      this.appInstallationTokenTtl = System.currentTimeMillis() + 3540000 // refresh in 59 minutes

      this.githubClient = GitHubBuilder().withAppInstallationToken(appInstallationToken).build()
    }
  }

  /**
   * Generate the JWT to Authenticating as a GitHub App
   */
  @Throws(Exception::class)
  fun createJWT(): String {
    val signatureAlgorithm: SignatureAlgorithm = SignatureAlgorithm.RS256 // must use RS256

    //  Get the base64 encoded private key and use it to sign the JWT
    val keyBytes: ByteArray = Decoders.BASE64.decode(System.getenv("GITHUB_APP_KEY"))
    val spec = PKCS8EncodedKeySpec(keyBytes)
    val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
    val signingKey: Key = keyFactory.generatePrivate(spec)

    val nowMillis = System.currentTimeMillis() - 30000 // rollback 30s to allow for clock drift
    val issuedAt = Date(nowMillis)
    val ttlMillis = nowMillis + 600000 // GitHub's JWT expiration time has 10m maximum
    val expiration = Date(ttlMillis)

    return Jwts.builder()
      .setIssuedAt(issuedAt)
      .setIssuer(System.getenv("GITHUB_APP_ID")) // use the GitHub App's ID as the value for the JWT issuer claim
      .signWith(signingKey, signatureAlgorithm)
      .setExpiration(expiration)
      .compact()
  }

  private fun refreshAppToken() {
    // refresh app installation token if necessary
    if (this.appInstallationTokenTtl > 0 && this.appInstallationTokenTtl < System.currentTimeMillis()) {
      initGithubClient()
    }
  }

  private fun findMatchingRepos(subject: PolicySubjectMatchers): List<GHRepository> {
    val searchRequest = githubClient.searchRepositories()

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

    return searchRequest.q(q).list().toList()
  }

  /**
   * Run validation over all rules in the policy.
   */
  fun validate(): List<PolicyValidationResult> {
    return policy.rules.flatMap { rule ->
      val repos = findMatchingRepos(rule.subject)
      repos.flatMap { repo ->
        // refresh app installation token if necessary
        refreshAppToken()

        // Determine if the repo is archived, if so, skip it
        if (repo.isArchived) {
          emptyList()
        } else if (rule.subject.exclude != null && repo.name in rule.subject.exclude) {
          emptyList()
        } else {
          if (logging) {
            System.err.println("Evaluating ${repo.name}...")
          }
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
      val repos = findMatchingRepos(rule.subject)
      repos.flatMap { repo ->
        // refresh app installation token if necessary
        refreshAppToken()

        // Determine if the repo is archived, if so, skip it
        if (repo.isArchived) {
          emptyList()
        } else if (rule.subject.exclude != null && repo.name in rule.subject.exclude) {
          emptyList()
        } else {
          if (logging) {
            System.err.println("Evaluating ${repo.name}...")
          }
          // Get the full repo — search results are abbreviated
          val fullRepo = this.githubClient.getRepository(repo.fullName)
          rule.operators.map { operator ->
            operator.enforce(fullRepo, this.githubClient)
          }
        }
      }
    }
  }
}
