package com.example.to_do_list

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class Task(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)

class ListViewModel : ViewModel() {

    private var nextId = 0
    var lastNotifiedId = -1

    private val _task = MutableStateFlow("")
    val task: StateFlow<String> = _task

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    fun updateTaskText(newText: String) {
        _task.value = newText
    }

    fun addTask() {
        if (_task.value.isNotBlank()) {
            val newTask = Task(id = nextId++, title = _task.value)
            _tasks.update { it + newTask }
            _task.value = "" // ekledikten sonra temizle
        }
    }

    fun removeTask(task: Task) {
        _tasks.update { it - task }
    }

    fun toggleDone(task: Task) {
        _tasks.update { list ->
            list.map {
                if (it.id == task.id) it.copy(isDone = !it.isDone) else it
            }
        }
    }

    fun getFirstIncompleteTask(): Task? {
        return _tasks.value.firstOrNull { !it.isDone }
    }

    fun getTaskById(id: Int): Task? {
        return _tasks.value.find { it.id == id }
    }

    fun updateTaskTitle(id: Int, newTitle: String) {
        val updated = _tasks.value.map { t ->
            if (t.id == id) t.copy(title = newTitle) else t
        }
        _tasks.value = updated
        if (lastNotifiedId == id) {
            lastNotifiedId = -1
        }
    }

    fun changeLastNotified(id: Int){
        lastNotifiedId = id
    }
}
