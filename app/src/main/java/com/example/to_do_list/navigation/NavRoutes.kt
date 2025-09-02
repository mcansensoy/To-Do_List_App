package com.example.to_do_list.navigation

object NavRoutes {
    const val LIST = "list"
    const val EDIT = "edit/{taskId}"

    fun editRoute(taskId: Int) = "edit/$taskId"
}