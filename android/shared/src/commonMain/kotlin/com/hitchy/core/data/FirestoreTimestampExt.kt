package com.hitchy.core.data

import dev.gitlive.firebase.firestore.Timestamp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Timestamp.toIsoString(): String = Instant.fromEpochSeconds(seconds, nanoseconds).toString()

@OptIn(ExperimentalTime::class)
fun Timestamp.toEpochMs(): Long = Instant.fromEpochSeconds(seconds, nanoseconds).toEpochMilliseconds()
