package com.example.android.videoplayersample.util

import java.util.concurrent.Executors

/**
 * @author Leopold
 */
private val IO_EXECUTOR = Executors.newSingleThreadExecutor()

fun ioThread(f : () -> Unit) = IO_EXECUTOR.execute(f)