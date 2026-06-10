package com.example.momentquest.model

sealed class TimelineItem {
    abstract val itemId: String
    abstract val timestamp: Long

    data class ChallengeItem(val challenge: Challenge) : TimelineItem() {
        override val itemId: String get() = challenge.id
        override val timestamp: Long get() = challenge.createdAt
    }

    data class MomentItem(val moment: Moment) : TimelineItem() {
        override val itemId: String get() = moment.id
        override val timestamp: Long get() = moment.createdAt
    }
}
