package com.louis.app.cavity.model

// Using this as a direct contract with API, need to be in snake case
@Suppress("PropertyName")
data class SyncAccountContent(
    val wine: List<Wine> = emptyList(),
    val bottle: List<Bottle> = emptyList(),
    val county: List<County> = emptyList(),
    val friend: List<Friend> = emptyList(),
    val grape: List<Grape> = emptyList(),
    val f_review: List<FReview> = emptyList(),
    val history_entry: List<HistoryEntry> = emptyList(),
    val history_x_friend: List<HistoryXFriend> = emptyList(),
    val q_grape: List<QGrape> = emptyList(),
    val review: List<Review> = emptyList(),
    val tag: List<Tag> = emptyList(),
    val tag_x_bottle: List<TagXBottle> = emptyList(),
    val tasting: List<Tasting> = emptyList(),
    val tasting_action: List<TastingAction> = emptyList(),
    val tasting_x_friend: List<TastingXFriend> = emptyList()
)
