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
    val mockBranch = mockk<GHBranch>()
    val mockProtection = mockk<GHBranchProtection>()

    if (! branchExists) {
      every { mockRepo.getBranch("unit-tests") } returns null
      return sut.validate(mockRepo)
    } else {
      every { mockRepo.getBranch("unit-tests") } returns mockBranch
    }

    if (! currentEnabled) {
      every { mockBranch.isProtected } returns false
      return sut.validate(mockRepo)
    } else {
      every { mockBranch.isProtected } returns true
      every { mockBranch.protection } returns mockProtection
    }

    val mockRequiredStatusChecks = if (currentRequiredChecks == null || currentRequireBranchUpToDate == null) {
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

    return sut.validate(mockRepo)
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
}
