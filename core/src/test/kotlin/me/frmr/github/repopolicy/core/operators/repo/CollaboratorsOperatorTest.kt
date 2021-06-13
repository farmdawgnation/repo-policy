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
class CollaboratorsOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

//  fun runValidate(desiredCollaborators: List<String>, currentCollaborators: Set<String>): PolicyValidationResult {
//    val sut = CollaboratorsOperator(desiredCollaborators)
//    every { mockRepo.collaboratorNames } returns currentCollaborators
//    every { mockRepo.fullName } returns "unit-tests/unit-tests"
//    return sut.validate(mockRepo)
//  }
//
//  @Test
//  fun validateFailsWithMissingCollaborators() {
//    val desiredCollaborators = listOf("thing1", "thing2")
//    val currentCollaborators = setOf("drseuss", "thing1")
//    val result = runValidate(desiredCollaborators, currentCollaborators)
//
//    assertThat(result.passed).isFalse
//    assertThat(result.description).isEqualTo("The following collaborators are missing: thing2")
//  }
//
//  @Test
//  fun validatePassesWithAllCollaboratorsPresent() {
//    val desiredCollaborators = listOf("thing1", "thing2")
//    val currentCollaborators = setOf("drseuss", "thing1", "thing2")
//    val result = runValidate(desiredCollaborators, currentCollaborators)
//
//    assertThat(result.passed).isTrue
//    assertThat(result.description).isEqualTo("All desired collaborators are present")
//  }
}
