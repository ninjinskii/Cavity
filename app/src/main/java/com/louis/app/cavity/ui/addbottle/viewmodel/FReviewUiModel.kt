package com.louis.app.cavity.ui.addbottle.viewmodel

import com.louis.app.cavity.db.dao.FReviewAndReview

data class FReviewUiModel(val reviewId: Long, val name: String, var type: Int, var value: Int) {
    companion object {
        fun fromFReview(fReview: FReviewAndReview): FReviewUiModel {
            return FReviewUiModel(
                fReview.review.id,
                fReview.review.contestName,
                fReview.review.type,
                fReview.fReview.value
            )
        }
    }
}

data class ReviewUiModel(val id: Long, val name: String, val type: Int, var isChecked: Boolean)
