package me.frmr.github.repopolicy.core.operators.repo

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GitHub

@ExtendWith(MockKExtension::class)
class VisibilityOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(desiredVisibility: String, currentVisibility: String): PolicyValidationResult {
    val mockGitHub = mockk<GitHub>()
    val sut = VisibilityOperator(desiredVisibility)
    every { mockRepo.isPrivate } returns (currentVisibility == "private")
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    return sut.validate(mockRepo, mockGitHub)
  }

  @Test
  fun validateFailsWhenVisibilityMismatches() {
    val result1 = runValidate("public", "private")
    assertThat(result1.passed).isFalse

    val result2 = runValidate("private", "public")
    assertThat(result2.passed).isFalse
  }

  @Test
  fun validateFailsWhenVisibilityMatches() {
    val result = runValidate("public", "public")
    assertThat(result.passed).isTrue
  }
}
