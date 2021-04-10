package com.louis.app.cavity.ui.addbottle.viewmodel

import com.louis.app.cavity.model.relation.grape.QuantifiedGrapeAndGrape

data class QGrapeUiModel(val grapeId: Long, val name: String, var percentage: Int) {
    companion object {
        fun fromQGrape(qGrape: QuantifiedGrapeAndGrape): QGrapeUiModel {
            return QGrapeUiModel(qGrape.qGrape.grapeId, qGrape.grapeName, qGrape.qGrape.percentage)
        }
    }
}

data class GrapeUiModel(val id: Long, val name: String, var isChecked: Boolean)
