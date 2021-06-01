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

@ExtendWith(MockKExtension::class)
class DeleteBranchOnMergeOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(desired: Boolean, current: Boolean): PolicyValidationResult {
    val sut = DeleteBranchOnMergeOperator(desired)
    every { mockRepo.isDeleteBranchOnMerge } returns current
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    return sut.validate(mockRepo)
  }

  fun runEnforce(desired: Boolean, current: Boolean): PolicyEnforcementResult {
    val sut = DeleteBranchOnMergeOperator(desired)
    every { mockRepo.isDeleteBranchOnMerge } returns current
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    every { mockRepo.deleteBranchOnMerge(desired) } returns Unit
    return sut.enforce(mockRepo)
  }

  @Test
  fun validationFailsWhenDesiredAndCurrentMismatch() {
    val result = runValidate(true, false)
    assertThat(result.passed).isFalse
    assertThat(result.description).isEqualTo("Delete branch false, should be true")
  }

  @Test
  fun validationPassesWhenDesiredAndCurrentMatch() {
    val result = runValidate(true, true)
    assertThat(result.passed).isTrue
    assertThat(result.description).isEqualTo("Delete branch on merge matches policy")
  }

  @Test
  fun enforcementDoesNothingWhenDesiredAndCurrentMatch() {
    val result = runEnforce(true, true)
    assertThat(result.passedValidation).isTrue
    assertThat(result.policyEnforced).isFalse
    assertThat(result.description).isEqualTo("Delete branch on merge matches policy")
  }

  @Test
  fun enforcementEnforcesWhenDesiredAndCurrentMismatch() {
    val result = runEnforce(true, false)
    assertThat(result.passedValidation).isFalse
    assertThat(result.policyEnforced).isTrue
    assertThat(result.description).isEqualTo("Delete branch on merge set to true")
  }
}
