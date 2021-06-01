package me.frmr.github.repopolicy.core.operators.repo

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.GHRepository

@ExtendWith(MockKExtension::class)
class DefaultBranchOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(desiredDefault: String, currentDefault: String): PolicyValidationResult {
    val sut = DefaultBranchOperator(desiredDefault)
    every { mockRepo.defaultBranch } returns currentDefault
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    return sut.validate(mockRepo)
  }

  @Test
  fun validationFailsWithMismatchBetweenDesiredAndCurrent() {
    val result = runValidate("main", "master")
    assertThat(result.passed).isFalse
    assertThat(result.description).isEqualTo("Default branch is master, should be main")
  }

  @Test
  fun validationPassesWhenMatch() {
    val result = runValidate("main", "main")
    assertThat(result.passed).isTrue
    assertThat(result.description).isEqualTo("Default branch is main")
  }
}
