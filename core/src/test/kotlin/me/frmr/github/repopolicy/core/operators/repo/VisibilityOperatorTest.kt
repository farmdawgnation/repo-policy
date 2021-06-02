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
class VisibilityOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(desiredVisibility: String, currentVisibility: String): PolicyValidationResult {
    val sut = VisibilityOperator(desiredVisibility)
    every { mockRepo.isPrivate } returns (currentVisibility == "private")
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    return sut.validate(mockRepo)
  }

  @Test
  fun validateFailsWhenVisibilityMismatches() {
    val result = runValidate("public", "private")
    assertThat(result.passed).isFalse
  }

  @Test
  fun validateFailsWhenVisibilityMatches() {
    val result = runValidate("public", "public")
    assertThat(result.passed).isTrue
  }
}