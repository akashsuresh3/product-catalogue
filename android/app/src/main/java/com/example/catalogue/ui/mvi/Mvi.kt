package com.example.catalogue.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface UiState
interface UiIntent
interface UiEffect

/**
 * One-way data flow:  UI --Intent--> ViewModel --reduce--> State --> UI
 * One-off events (navigation, snackbar) go out as Effects so they are not replayed on rotation.
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    protected val currentState: S get() = _state.value

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    /** The single entry point for everything the user does. */
    abstract fun onIntent(intent: I)

    protected fun setState(reduce: S.() -> S) = _state.update { it.reduce() }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

/** Loading / content / error for any piece of remote data. */
sealed interface Async<out T> {
    data object Loading : Async<Nothing>
    data class Success<T>(val data: T) : Async<T>
    data class Failure(val error: ErrorKind) : Async<Nothing>
}

/** UI-facing error categories; screens map these to strings. */
enum class ErrorKind { Network, NotFound, Server, Unknown }
