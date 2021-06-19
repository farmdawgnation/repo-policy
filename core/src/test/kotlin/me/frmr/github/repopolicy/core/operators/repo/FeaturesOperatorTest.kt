package me.frmr.github.repopolicy.core.operators.repo

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.frmr.github.repopolicy.core.model.PolicyEnforcementResult
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

@ExtendWith(MockKExtension::class)
class FeaturesOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(desiredIssues: Boolean?, currentIssues: Boolean, desiredProjects: Boolean?, currentProjects: Boolean, desiredWiki: Boolean?, currentWiki: Boolean): PolicyValidationResult {
    val mockGithub = mockk<GitHub>()
    val sut = FeaturesOperator(desiredIssues, desiredProjects, desiredWiki)
    every { mockRepo.fullName } returns "unit-tests/unit-tests"

    if (desiredIssues != null) {
      every { mockRepo.hasIssues() } returns currentIssues
    }

    if (desiredProjects != null) {
      every { mockRepo.hasProjects() } returns currentProjects
    }

    if (desiredWiki != null) {
      every { mockRepo.hasWiki() } returns currentWiki
    }

    return sut.validate(mockRepo, mockGithub)
  }

  fun runEnforce(desiredIssues: Boolean?, currentIssues: Boolean, desiredProjects: Boolean?, currentProjects: Boolean, desiredWiki: Boolean?, currentWiki: Boolean): PolicyEnforcementResult {
    val sut = FeaturesOperator(desiredIssues, desiredProjects, desiredWiki)
    every { mockRepo.fullName } returns "unit-tests/unit-tests"

    if (desiredIssues != null) {
      every { mockRepo.hasIssues() } returns currentIssues
      every { mockRepo.enableIssueTracker(desiredIssues) } returns Unit
    }

    if (desiredProjects != null) {
      every { mockRepo.hasProjects() } returns currentProjects
      every { mockRepo.enableProjects(desiredProjects) } returns Unit
    }

    if (desiredWiki != null) {
      every { mockRepo.hasWiki() } returns currentWiki
      every { mockRepo.enableWiki(desiredWiki) } returns Unit
    }

    return sut.enforce(mockRepo, mockk<GitHub>())
  }

  @Test
  fun validationFailsWithPartialRequestsMismatching() {
    val result = runValidate(
      null,
      true,
      true,
      false,
      false,
      true
    )

    assertThat(result.passed).isFalse
    assertThat(result.description).isEqualTo("policy requires projects be enabled, found disabled, policy requires wikis be disabled, found enabled")
  }

  @Test
  fun validationFailsWithFullRequestsMismatching() {
    val result = runValidate(
      true,
      false,
      true,
      false,
      false,
      true
    )

    assertThat(result.passed).isFalse
    assertThat(result.description).isEqualTo("policy requires issues be enabled, found disabled, policy requires projects be enabled, found disabled, policy requires wikis be disabled, found enabled")
  }

  @Test
  fun validationPassesWithPartialRequestsMatching() {
    val result = runValidate(
      null,
      true,
      true,
      true,
      false,
      false
    )

    assertThat(result.passed).isTrue
    assertThat(result.description).isEqualTo("Enabled features match policy")
  }

  @Test
  fun validationPassesWithFullRequestsMatching() {
    val result = runValidate(
      true,
      true,
      true,
      true,
      false,
      false
    )

    assertThat(result.passed).isTrue
    assertThat(result.description).isEqualTo("Enabled features match policy")
  }

  @Test
  fun enforcementWorksAsExpectedWhenValidationFails() {
    val result = runEnforce(
      true,
      false,
      true,
      false,
      true,
      true
    )

    assertThat(result.passedValidation).isFalse
    assertThat(result.policyEnforced).isTrue
  }

  @Test
  fun enforcementWorksAsExpectedWhenValidationPasses() {
    val result = runEnforce(
      false,
      false,
      true,
      true,
      true,
      true
    )

    assertThat(result.passedValidation).isTrue
    assertThat(result.policyEnforced).isFalse
  }
}
