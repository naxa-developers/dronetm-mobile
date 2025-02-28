package np.com.naxa.drone_tasking_manager.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Executes a given [action] with a debounced effect based on the [input] value.
 *
 * This composable function is designed to prevent the rapid execution of an action
 * when the input changes frequently. It uses a debounce mechanism to wait for a period
 * of inactivity (defined by [debounceMillis]) before executing the [action].
 *
 * **How it Works:**
 * 1. It observes the [input] value for changes.
 * 2. Whenever the [input] changes, it starts a timer of [debounceMillis].
 * 3. If the [input] changes again within the [debounceMillis] period, the timer restarts.
 * 4. Only when the [input] remains unchanged for the duration of [debounceMillis] does the [action] get executed.
 * 5. The `delay` inside the `collect` is a correction to ensure that the action is called after the debounce period,
 *    as the `debounce` only emits after the delay, not execute the operation after.
 *
 * **Use Cases:**
 * - Search bars: Avoid performing a search on every keystroke; instead, perform it after the user has paused typing.
 * - Button clicks: Prevent accidental double-clicks by debouncing the action.
 * - Resizing events: Avoid performing resource-intensive layouts on every minor resize; instead, perform it after the resizing has stopped.
 * - Updating values: When a value is changed frequently, this can avoid too many executions of the action.
 *
 * @param T The type of the input value.
 * @param input The lambda that return input value that triggers the debounced action.
 * @param debounceMillis The duration (in milliseconds) to wait for inactivity before executing the action. Defaults to 300ms.
 * @param action The action to execute after the debounce period. This is a suspend function that takes the input value as a parameter.
 */
@OptIn(FlowPreview::class)
@Composable
fun <T> DebouncedAction(
    input: () -> T,
    debounceMillis: Long = 300L,
    action: suspend (T) -> Unit
) {
    LaunchedEffect(Unit) {
        snapshotFlow { input() }
            .debounce(debounceMillis)
            .collect { value ->
//                delay(10L)
                action(value)
            }
    }
}