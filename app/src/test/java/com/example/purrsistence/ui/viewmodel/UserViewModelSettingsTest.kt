package com.example.purrsistence.ui.viewmodel

import com.example.purrsistence.data.local.repository.FakeUserRepository
import com.example.purrsistence.domain.service.fakes.FakeSupabaseSyncService
import com.example.purrsistence.service.ShopService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelSettingsTest {

    private val dispatcher = StandardTestDispatcher()
    private val fakeSupabaseSyncService = FakeSupabaseSyncService()
    private val fakeUserRepository = FakeUserRepository()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateUsernameInSupabase_callsSyncService() = runTest {
        val shopService = ShopService(fakeUserRepository)
        val viewModel = UserViewModel(
            shopService = shopService,
            supabaseSyncService = fakeSupabaseSyncService
        )

        val newUsername = "new_cat_lover"
        viewModel.updateUsernameInSupabase(newUsername)
        
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeSupabaseSyncService.updateUsernameCalls)
        assertEquals(newUsername, fakeSupabaseSyncService.lastUsername)
    }

    @Test
    fun updatePasswordInSupabase_callsSyncService() = runTest {
        val shopService = ShopService(fakeUserRepository)
        val viewModel = UserViewModel(
            shopService = shopService,
            supabaseSyncService = fakeSupabaseSyncService
        )

        val currentPass = "old_pass"
        val newPass = "new_pass"
        viewModel.updatePasswordInSupabase(currentPass, newPass)

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeSupabaseSyncService.updatePasswordCalls)
        assertEquals(currentPass, fakeSupabaseSyncService.lastCurrentPassword)
        assertEquals(newPass, fakeSupabaseSyncService.lastNewPassword)
    }
}
