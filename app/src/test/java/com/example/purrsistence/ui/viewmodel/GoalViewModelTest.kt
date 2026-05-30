package com.example.purrsistence.ui.viewmodel

import android.content.SharedPreferences
import com.example.purrsistence.data.local.repository.GoalRepository
import com.example.purrsistence.domain.service.fakes.FakeSupabaseSyncService
import com.example.purrsistence.domain.time.TimeProvider
import com.example.purrsistence.service.GoalService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

// Very small fake SharedPreferences for unit tests
private class FakeSharedPreferences(initial: Map<String, Int> = emptyMap()) : SharedPreferences {
    private val data = initial.toMutableMap()

    override fun getAll(): MutableMap<String, *> = data
    override fun getString(key: String?, defValue: String?): String? = null
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
    override fun getInt(key: String?, defValue: Int): Int = data[key] ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = defValue
    override fun getFloat(key: String?, defValue: Float): Float = defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class Editor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Int?>()
        override fun putString(key: String?, value: String?): SharedPreferences.Editor = this
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key != null) pending[key] = value
            return this
        }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = this
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = this
        override fun remove(key: String?): SharedPreferences.Editor { if (key != null) pending[key] = null; return this }
        override fun clear(): SharedPreferences.Editor { data.clear(); return this }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() { pending.forEach { (k, v) -> if (v == null) data.remove(k) else data[k] = v } ; pending.clear() }
    }
}

// Minimal fake implementations for GoalService dependencies - not used actively in these tests
private class FakeGoalRepository : GoalRepository {
    override fun getGoals(userId: Int) = kotlinx.coroutines.flow.flowOf<List<com.example.purrsistence.domain.model.GoalWithSessions>>(emptyList())
    override fun getActiveGoals(userId: Int) = kotlinx.coroutines.flow.flowOf<List<com.example.purrsistence.domain.model.GoalWithSessions>>(emptyList())
    override suspend fun insertGoal(goal: com.example.purrsistence.domain.model.Goal) {}
    override suspend fun deleteGoal(goalId: Int) {}
    override fun getGoal(goalId: Int?) = kotlinx.coroutines.flow.flowOf<com.example.purrsistence.domain.model.Goal?>(null)
    override fun getGoalWithSessions(goalId: Int?) = kotlinx.coroutines.flow.flowOf<com.example.purrsistence.domain.model.GoalWithSessions?>(null)
    override suspend fun updateGoal(goal: com.example.purrsistence.domain.model.Goal) {}
    override fun searchGoals(userId: Int, query: String) = kotlinx.coroutines.flow.flowOf<List<com.example.purrsistence.domain.model.GoalWithSessions>>(emptyList())
    override suspend fun getInactiveGoals(): List<com.example.purrsistence.domain.model.Goal> = emptyList()
    override suspend fun getGoalsForSync(userId: Int): List<com.example.purrsistence.domain.model.Goal> = emptyList()
    override suspend fun resetGoalsStatus(userId: Int) {}
    override suspend fun replaceGoalsFromRemoteSync(userId: Int, goals: List<com.example.purrsistence.domain.model.Goal>) {}
}

private class FakeTimeProvider : TimeProvider { override fun now(): Instant = Instant.EPOCH }

class GoalViewModelTest {

    @Test
    fun `init loads saved selected goal id from prefs`() {
        val prefs = FakeSharedPreferences(mapOf("selected_goal_id" to 5))
        val goalService = GoalService(FakeGoalRepository(), FakeTimeProvider())
        val syncService = FakeSupabaseSyncService()

        val vm = GoalViewModel(goalService, prefs, syncService)

        assertEquals(5, vm.selectedGoalId)
    }

    @Test
    fun `selectGoal updates state and saves to prefs`() = runBlocking {
        val prefs = FakeSharedPreferences()
        val goalService = GoalService(FakeGoalRepository(), FakeTimeProvider())
        val syncService = FakeSupabaseSyncService()

        val vm = GoalViewModel(goalService, prefs, syncService)
        vm.selectGoal(12)

        assertEquals(12, vm.selectedGoalId)
        // ensure prefs persisted
        assertEquals(12, prefs.getInt("selected_goal_id", -1))
    }

    @Test
    fun `onSearchQueryChange updates searchQuery`() {
        val prefs = FakeSharedPreferences()
        val goalService = GoalService(FakeGoalRepository(), FakeTimeProvider())
        val syncService = FakeSupabaseSyncService()
        val vm = GoalViewModel(goalService, prefs, syncService)

        vm.onSearchQueryChange("read")
        assertEquals("read", vm.searchQuery)
    }
}

