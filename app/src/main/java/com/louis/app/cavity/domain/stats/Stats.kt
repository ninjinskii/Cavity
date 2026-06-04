package com.louis.app.cavity.domain.stats

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import androidx.room.TypeConverters
import com.louis.app.cavity.db.StatsBottleIdsTypeConverter
import com.louis.app.cavity.model.Grape
import com.louis.app.cavity.model.QGrape
import com.louis.app.cavity.model.WineColor
import com.louis.app.cavity.util.ColorUtil

interface Stat {
    val label: String
    val count: Int
    val percentage: Float
    val color: Int
    val bottleIds: List<Long>
}

data class BaseStat(
    override val label: String,
    override val count: Int,
    override val percentage: Float,
    @field:TypeConverters(StatsBottleIdsTypeConverter::class) override val bottleIds: List<Long>
) : Stat {
    @Ignore
    override val color = ColorUtil.next()
}

data class WineColorStat(
    val wcolor: WineColor,
    override val count: Int,
    override val percentage: Float,
    @field:TypeConverters(StatsBottleIdsTypeConverter::class) override val bottleIds: List<Long>
) : Stat {
    @Ignore
    override val label = wcolor.name

    @Ignore
    override val color = ColorUtil.getColorResForWineColor(wcolor.ordinal)
}

data class QGrapeAndGrape(
    @Embedded val qGrape: QGrape,
    @Relation(
        entity = Grape::class,
        parentColumn = "grape_id",
        entityColumn = "id",
        projection = ["name"]
    )
    val grapeName: String
) :
    Stat {
    @Ignore
    override val percentage = qGrape.percentage.toFloat()

    @Ignore
    override val count = -1

    @Ignore
    override val label = grapeName

    @Ignore
    override val color = ColorUtil.next()

    @Ignore
    override val bottleIds = emptyList<Long>()
}

data class PriceByCurrency(
    val sum: Long,
    val currency: String
) {
    override fun toString(): String {
        return "$sum $currency"
    }
}

data class BottleStatRow(
    val bottleId: Long,
    val naming: String?,
    val vintage: String?,
    val price: Double,
    val currency: String?,
)
