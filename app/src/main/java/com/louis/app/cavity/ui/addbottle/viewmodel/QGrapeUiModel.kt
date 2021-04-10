package com.louis.app.cavity.ui.addbottle.viewmodel

import com.louis.app.cavity.model.relation.grape.QuantifiedGrapeAndGrape

data class QGrapeUiModel(val name: String, var percentage: Int) {
    companion object {
        fun fromQGrape(qGrape: QuantifiedGrapeAndGrape): QGrapeUiModel {
            return QGrapeUiModel(qGrape.grapeName, qGrape.qGrape.percentage)
        }
    }
}

data class GrapeUiModel(val name: String, var isChecked: Boolean)
