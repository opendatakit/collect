package org.odk.collect.android.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.android.utilities.AdminPasswordProvider

class ProjectPreferencesViewModel(adminPasswordProvider: AdminPasswordProvider) : ViewModel() {
    enum class State {
        LOCKED, // Admin password is set but not entered
        UNLOCKED, // Admin password is set and entered
        NOT_PROTECTED // Admin password is not set
    }

    private var state: State

    init {
        state = if (adminPasswordProvider.isAdminPasswordSet) {
            State.LOCKED
        } else {
            State.NOT_PROTECTED
        }
    }

    fun isStateLocked() = state == State.LOCKED

    fun isStateUnlocked() = state == State.UNLOCKED

    fun setStateLocked() {
        state = State.LOCKED
    }

    fun setStateUnlocked() {
        state = State.UNLOCKED
    }

    fun setStateNotProtected() {
        state = State.NOT_PROTECTED
    }

    open class Factory(private val adminPasswordProvider: AdminPasswordProvider) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ProjectPreferencesViewModel(adminPasswordProvider) as T
        }
    }
}
