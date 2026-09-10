package com.example.ui

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> awaitTask(task: Task<T>): T = suspendCoroutine { cont ->
    task.addOnSuccessListener { cont.resume(it) }
    task.addOnFailureListener { cont.resumeWithException(it) }
}
