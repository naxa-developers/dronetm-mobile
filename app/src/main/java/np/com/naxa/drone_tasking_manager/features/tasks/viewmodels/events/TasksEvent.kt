package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events


sealed class TasksEvent {
    /**
     * Represents an event indicating that a task should be unlocked.
     *
     * This event is typically used to signal that a task, previously locked
     * (e.g., for editing or processing), is now available for modification
     * or further actions.  It contains the ID of the task to unlock and
     * the ID of the project it belongs to.
     *
     * @property taskId The unique identifier of the task to be unlocked.
     * @property projectId The unique identifier of the project to which the task belongs.
     *
     * This class extends [TasksEvent], indicating that it is a specific type of
     * event related to tasks.
     */


    data class UnlockTask(val taskId: String, val projectId: String): TasksEvent()
    /**
     * Represents a task locking event.
     *
     * This data class is used to encapsulate the information related to a task
     * being locked. It includes the unique identifier of the task and the
     * project to which the task belongs.
     *
     * @property taskId The unique identifier of the task that was locked.
     * @property projectId The unique identifier of the project to which the locked task belongs.
     */
    data class LockTask(val taskId: String, val projectId: String): TasksEvent()
}