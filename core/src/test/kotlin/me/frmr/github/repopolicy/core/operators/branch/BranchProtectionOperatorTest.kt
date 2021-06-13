package me.frmr.github.repopolicy.core.operators.branch

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.frmr.github.repopolicy.core.model.PolicyValidationResult
import org.junit.jupiter.api.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.kohsuke.github.*

@ExtendWith(MockKExtension::class)
class BranchProtectionOperatorTest {
  @MockK
  lateinit var mockRepo: GHRepository

  @BeforeEach
  fun clearMyMocks() {
    clearMocks(mockRepo)
  }

  fun runValidate(
    desiredEnabled: Boolean = false,
    currentEnabled: Boolean = false,
    desiredRequiredChecks: List<String>? = null,
    currentRequiredChecks: List<String>? = null,
    desiredDismissStaleReviews: Boolean? = null,
    currentDismissStaleReviews: Boolean? = null,
    desiredIncludeAdmins: Boolean? = null,
    currentIncludeAdmins: Boolean? = null,
    desiredRequireBranchUpToDate: Boolean? = null,
    currentRequireBranchUpToDate: Boolean? = null,
    desiredRequireCodeOwnerReviews: Boolean? = null,
    currentRequireCodeOwnerReviews: Boolean? = null,
    desiredRequiredReviewCount: Int? = null,
    currentRequiredReviewCount: Int? = null,
    desiredRestrictPushAccess: Boolean? = null,
    currentRestrictPushAccess: Boolean? = null,
    desiredRestrictReviewDismissals: Boolean? = null,
    currentRestrictReviewDismissals: Boolean? = null,
    desiredPushTeams: List<String>? = null,
    currentPushTeams: List<String> = emptyList(),
    desiredPushUsers: List<String>? = null,
    currentPushUsers: List<String> = emptyList(),
    desiredReviewDismissalUsers: List<String>? = null,
    currentReviewDismissalUsers: List<String> = emptyList(),
    branchExists: Boolean = true,
  ): PolicyValidationResult {
    val sut = BranchProtectionOperator(
      branch = "unit-tests",
      enabled = desiredEnabled,
      requiredChecks = desiredRequiredChecks,
      dismissStaleReviews = desiredDismissStaleReviews,
      includeAdmins = desiredIncludeAdmins,
      requireBranchIsUpToDate = desiredRequireBranchUpToDate,
      requireCodeOwnerReviews = desiredRequireCodeOwnerReviews,
      requiredReviewCount = desiredRequiredReviewCount,
      restrictPushAccess = desiredRestrictPushAccess,
      restrictReviewDismissals = desiredRestrictReviewDismissals,
      pushTeams = desiredPushTeams,
      pushUsers = desiredPushUsers,
      reviewDismissalUsers = desiredReviewDismissalUsers,
    )
    every { mockRepo.fullName } returns "unit-tests/unit-tests"
    val mockGithub = mockk<GitHub>()
    val mockBranch = mockk<GHBranch>()
    val mockProtection = mockk<GHBranchProtection>()

    if (! branchExists) {
      every { mockRepo.getBranch("unit-tests") } returns null
      return sut.validate(mockRepo, mockGithub)
    } else {
      every { mockRepo.getBranch("unit-tests") } returns mockBranch
    }

    if (! currentEnabled) {
      every { mockBranch.isProtected } returns false
      return sut.validate(mockRepo, mockGithub)
    } else {
      every { mockBranch.isProtected } returns true
      every { mockBranch.getProtection() }.answers { mockProtection }
    }

    val mockRequiredStatusChecks = if (currentRequiredChecks == null && currentRequireBranchUpToDate == null) {
      null
    } else {
      val mrsc = mockk<GHBranchProtection.RequiredStatusChecks>()
      every { mrsc.contexts } returns currentRequiredChecks
      every { mrsc.isRequiresBranchUpToDate } returns (currentRequireBranchUpToDate ?: false)
      mrsc
    }

    every { mockProtection.requiredStatusChecks } returns mockRequiredStatusChecks

    val mockRequiredReviews = mockk<GHBranchProtection.RequiredReviews>()
    every { mockRequiredReviews.isDismissStaleReviews } returns (currentDismissStaleReviews ?: false)
    every { mockRequiredReviews.isRequireCodeOwnerReviews } returns (currentRequireCodeOwnerReviews ?: false)
    every { mockRequiredReviews.requiredReviewers } returns (currentRequiredReviewCount ?: 0)

    every { mockProtection.requiredReviews } returns mockRequiredReviews

    val mockEnforceAdmins = mockk<GHBranchProtection.EnforceAdmins>()
    every { mockEnforceAdmins.isEnabled } returns (currentIncludeAdmins ?: false)
    every { mockProtection.enforceAdmins } returns mockEnforceAdmins


    val mockRestrictions = mockk<GHBranchProtection.Restrictions>()
    val mockGhTeams = currentPushTeams.map { teamName ->
      val mght = mockk<GHTeam>()
      every { mght.name } returns teamName
      mght
    }
    every { mockRestrictions.teams } returns mockGhTeams

    val mockGhUsers = currentPushUsers.map { userName ->
      val mghu = mockk<GHUser>()
      every { mghu.name } returns userName
      mghu
    }
    every { mockRestrictions.users } returns mockGhUsers

    if (currentRestrictPushAccess == true) {
      every { mockProtection.restrictions } returns mockRestrictions
    } else {
      every { mockProtection.restrictions } returns null
    }

    val mockDismissalRestrictions = mockk<GHBranchProtection.Restrictions>()
    val mockGhDismissalUsers = currentReviewDismissalUsers.map { userName ->
      val mghu = mockk<GHUser>()
      every { mghu.name } returns userName
      mghu
    }
    every { mockDismissalRestrictions.users } returns mockGhDismissalUsers
    if (currentRestrictReviewDismissals == true) {
      every { mockRequiredReviews.dismissalRestrictions } returns mockDismissalRestrictions
    } else {
      every { mockRequiredReviews.dismissalRestrictions } returns null
    }

    return sut.validate(mockRepo, mockGithub)
  }

  @Test
  fun handlesMissingBranch() {
    val result = runValidate(branchExists = false)
    assertThat(result.passed).isFalse
    assertThat(result.description).isEqualTo("Branch does not exist")
  }

  @Test
  fun failsOnProtectionMismatch() {
    val result1 = runValidate(desiredEnabled = false, currentEnabled = true)
    assertThat(result1.passed).isFalse
    assertThat(result1.description).isEqualTo("Branch protection enabled, should be disabled")

    val result2 = runValidate(desiredEnabled = true, currentEnabled = false)
    assertThat(result2.passed).isFalse
    assertThat(result2.description).isEqualTo("Branch protection not enabled")
  }

  @Test
  fun passesOnProtectionMatch() {
    val result1 = runValidate(desiredEnabled = true, currentEnabled = true)
    assertThat(result1.passed).isTrue
    assertThat(result1.description).isEqualTo("Branch protection matches policy")

    val result2 = runValidate(desiredEnabled = false, currentEnabled = false)
    assertThat(result2.passed).isTrue
    assertThat(result2.description).isEqualTo("Branch protection matches policy")
  }

  @Test
  fun failsOnMissingRequiredChecks() {
    val result = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequiredChecks = listOf("unit-tests", "integration-tests"),
      currentRequiredChecks = listOf("unit-tests")
    )

    assertThat(result.passed).isFalse
    assertThat(result.extra).isEqualTo("Missing required checks")
  }

  @Test
  fun passesOnMatchingRequiredChecks() {
    val result = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequiredChecks = listOf("unit-tests", "integration-tests"),
      currentRequiredChecks = listOf("unit-tests", "integration-tests")
    )

    assertThat(result.passed).isTrue
  }

  @Test
  fun failsOnDismissStaleReviewsMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredDismissStaleReviews = false,
      currentDismissStaleReviews = true,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Dismiss stale reviews enabled")

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredDismissStaleReviews = true,
      currentDismissStaleReviews = false,
    )

    assertThat(result2.passed).isFalse
    assertThat(result2.extra).isEqualTo("Dismiss stale reviews disabled")
  }

  @Test
  fun passesOnDismissStaleReviewsMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredDismissStaleReviews = false,
      currentDismissStaleReviews = false,
    )

    assertThat(result1.passed).isTrue

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredDismissStaleReviews = true,
      currentDismissStaleReviews = true,
    )

    assertThat(result2.passed).isTrue
  }

  @Test
  fun failsOnIncludeAdminsMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredIncludeAdmins = false,
      currentIncludeAdmins = true,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Include admins enabled")

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredIncludeAdmins = true,
      currentIncludeAdmins = false,
    )

    assertThat(result2.passed).isFalse
    assertThat(result2.extra).isEqualTo("Include admins disabled")
  }

  @Test
  fun passesOnIncludeAdminsMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredIncludeAdmins = false,
      currentIncludeAdmins = false,
    )

    assertThat(result1.passed).isTrue

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredIncludeAdmins = true,
      currentIncludeAdmins = true,
    )

    assertThat(result2.passed).isTrue
  }

  @Test
  fun failsOnRequireBranchUpToDateMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireBranchUpToDate = false,
      currentRequireBranchUpToDate = true,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Require branch up to date is enabled")

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireBranchUpToDate = true,
      currentRequireBranchUpToDate = false,
    )

    assertThat(result2.passed).isFalse
    assertThat(result2.extra).isEqualTo("Require branch up to date is disabled")
  }

  @Test
  fun passesOnRequireBranchUpToDateMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireBranchUpToDate = false,
      currentRequireBranchUpToDate = false,
    )

    assertThat(result1.passed).isTrue

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireBranchUpToDate = true,
      currentRequireBranchUpToDate = true,
    )

    assertThat(result2.passed).isTrue
  }

  @Test
  fun failsOnRequireCodeOwnerReviewsMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireCodeOwnerReviews = false,
      currentRequireCodeOwnerReviews = true,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Code owner reviews are required")

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireCodeOwnerReviews = true,
      currentRequireCodeOwnerReviews = false,
    )

    assertThat(result2.passed).isFalse
    assertThat(result2.extra).isEqualTo("Code owner reviews are not required")
  }

  @Test
  fun passesOnRequireCodeOwnerReviewsMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireCodeOwnerReviews = false,
      currentRequireCodeOwnerReviews = false,
    )

    assertThat(result1.passed).isTrue

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequireCodeOwnerReviews = true,
      currentRequireCodeOwnerReviews = true,
    )

    assertThat(result2.passed).isTrue
  }

  @Test
  fun failsOnRequiredReviewCountMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequiredReviewCount = 1,
      currentRequiredReviewCount = 0,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Requires 0 reviews, should require 1")
  }

  @Test
  fun passesOnRequiredReviewCountMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRequiredReviewCount = 1,
      currentRequiredReviewCount = 1,
    )

    assertThat(result1.passed).isTrue
  }

  @Test
  fun failsOnRestrictReviewDismissalsMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictReviewDismissals = false,
      currentRestrictReviewDismissals = true,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Review dismissal restrictions present")

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictReviewDismissals = true,
      currentRestrictReviewDismissals = false,
    )

    assertThat(result2.passed).isFalse
    assertThat(result2.extra).isEqualTo("Review dismissal restrictions missing")
  }

  @Test
  fun passesOnRestrictReviewDismissalsMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictReviewDismissals = false,
      currentRestrictReviewDismissals = false,
    )

    assertThat(result1.passed).isTrue

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictReviewDismissals = true,
      currentRestrictReviewDismissals = true,
    )

    assertThat(result2.passed).isTrue
  }

  @Test
  fun failsOnReviewDismissalUsersMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictReviewDismissals = true,
      currentRestrictReviewDismissals = true,
      desiredReviewDismissalUsers = listOf("thing1", "thing2"),
      currentReviewDismissalUsers = listOf("thing1")
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Missing users from review dismissal users")
  }

  @Test
  fun passesOnReviewDismissalUsersMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictReviewDismissals = true,
      currentRestrictReviewDismissals = true,
      desiredReviewDismissalUsers = listOf("thing1", "thing2"),
      currentReviewDismissalUsers = listOf("thing1", "thing2")
    )

    assertThat(result1.passed).isTrue
  }

  @Test
  fun failsOnRestrictPushAccessMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = false,
      currentRestrictPushAccess = true,
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Push restrictions found")

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = true,
      currentRestrictPushAccess = false,
    )

    assertThat(result2.passed).isFalse
    assertThat(result2.extra).isEqualTo("No push restrictions found")
  }

  @Test
  fun passesOnRestrictPushAccessMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = false,
      currentRestrictPushAccess = false,
    )

    assertThat(result1.passed).isTrue

    val result2 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = true,
      currentRestrictPushAccess = true,
    )

    assertThat(result2.passed).isTrue
  }

  @Test
  fun failsOnPushTeamsMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = true,
      currentRestrictPushAccess = true,
      desiredPushTeams = listOf("thing1", "thing2"),
      currentPushTeams = listOf("thing1")
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Teams missing from list of teams that can push")
  }

  @Test
  fun passesOnPushTeamsMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = true,
      currentRestrictPushAccess = true,
      desiredPushTeams = listOf("thing1", "thing2"),
      currentPushTeams = listOf("thing1", "thing2")
    )

    assertThat(result1.passed).isTrue
  }

  @Test
  fun failsOnPushUsersMismatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = true,
      currentRestrictPushAccess = true,
      desiredPushUsers = listOf("thing1", "thing2"),
      currentPushUsers = listOf("thing1")
    )

    assertThat(result1.passed).isFalse
    assertThat(result1.extra).isEqualTo("Users missing from list of users that can push")
  }

  @Test
  fun passesOnPushUsersMatch() {
    val result1 = runValidate(
      desiredEnabled = true,
      currentEnabled = true,
      desiredRestrictPushAccess = true,
      currentRestrictPushAccess = true,
      desiredPushUsers = listOf("thing1", "thing2"),
      currentPushUsers = listOf("thing1", "thing2")
    )

    assertThat(result1.passed).isTrue
  }
}
