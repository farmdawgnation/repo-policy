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
class PullRequestsOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(
          desiredAllowMergeCommit: Boolean?,
          currentAllowMergeCommit: Boolean,
          desiredAllowSquashMerge: Boolean?,
          currentAllowSquashMerge: Boolean,
          desiredAllowRebaseMerge: Boolean?,
          currentAllowRebaseMerge: Boolean
  ): PolicyValidationResult {
    val mockGithub = mockk<GitHub>()
    val sut = PullRequestsOperator(desiredAllowMergeCommit, desiredAllowSquashMerge, desiredAllowRebaseMerge)
    every { mockRepo.fullName } returns "unit-tests/unit-tests"

    if (desiredAllowMergeCommit != null) {
      every { mockRepo.isAllowMergeCommit } returns currentAllowMergeCommit
    }

    if (desiredAllowSquashMerge != null) {
      every { mockRepo.isAllowSquashMerge } returns currentAllowSquashMerge
    }

    if (desiredAllowRebaseMerge != null) {
      every { mockRepo.isAllowRebaseMerge } returns currentAllowRebaseMerge
    }

    return sut.validate(mockRepo, mockGithub)
  }

  fun runEnforce(
          desiredAllowMergeCommit: Boolean?,
          currentAllowMergeCommit: Boolean,
          desiredAllowSquashMerge: Boolean?,
          currentAllowSquashMerge: Boolean,
          desiredAllowRebaseMerge: Boolean?,
          currentAllowRebaseMerge: Boolean
  ): PolicyEnforcementResult {
    val mockGithub = mockk<GitHub>()
    val sut = PullRequestsOperator(desiredAllowMergeCommit, desiredAllowSquashMerge, desiredAllowRebaseMerge)
    every { mockRepo.fullName } returns "unit-tests/unit-tests"

    if (desiredAllowMergeCommit != null) {
      every { mockRepo.isAllowMergeCommit } returns currentAllowMergeCommit
      every { mockRepo.allowMergeCommit(desiredAllowMergeCommit) } returns Unit
    }

    if (desiredAllowSquashMerge != null) {
      every { mockRepo.isAllowSquashMerge } returns currentAllowSquashMerge
      every { mockRepo.allowSquashMerge(desiredAllowSquashMerge) } returns Unit
    }

    if (desiredAllowRebaseMerge != null) {
      every { mockRepo.isAllowRebaseMerge } returns currentAllowRebaseMerge
      every { mockRepo.allowRebaseMerge(desiredAllowRebaseMerge) } returns Unit
    }

    return sut.enforce(mockRepo, mockGithub)
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
    assertThat(result.description).isEqualTo("allow squash merging is disabled, should be enabled, allow rebase merging is enabled, should be disabled")
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
    assertThat(result.description).isEqualTo("allow merge commits is disabled, should be enabled, allow squash merging is disabled, should be enabled, allow rebase merging is enabled, should be disabled")
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
    assertThat(result.description).isEqualTo("Pull Request settings match policy")
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
    assertThat(result.description).isEqualTo("Pull Request settings match policy")
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
    assertThat(result.description).isEqualTo("Pull Request settings updated")
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
    assertThat(result.description).isEqualTo("Pull Request settings match policy")
  }

}