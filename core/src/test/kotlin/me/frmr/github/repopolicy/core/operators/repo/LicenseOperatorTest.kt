package me.frmr.github.repopolicy.core.operators.repo

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.GHLicense
import org.kohsuke.github.GHRepository

@ExtendWith(MockKExtension::class)
class LicenseOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(desiredLicense: String, currentLicense: String): PolicyValidationResult {
    val sut = LicenseOperator(desiredLicense)
    val licenseMock = mockk<GHLicense>()
    every { mockRepo.license } returns licenseMock
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    every { licenseMock.key } returns currentLicense
    return sut.validate(mockRepo)
  }

  @Test
  fun failsWhenLicensesMismatch() {
    val result = runValidate("mit", "proprietary")
    assertThat(result.passed).isFalse
    assertThat(result.description).isEqualTo("License was proprietary, needed mit.")
  }

  @Test
  fun passesWhenLicensesMatch() {
    val result = runValidate("mit", "mit")
    assertThat(result.passed).isTrue
    assertThat(result.description).isEqualTo("License check matches")
  }
}
