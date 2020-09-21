package com.louis.app.cavity.util

enum class AdviceType {
    MEDAL,
    RATE_20,
    RATE_100,
    STARS
}

enum class MedalColor(color: Int) {
    BRONZE(0),
    SILVER(1),
    GOLD(2)
}

enum class Stars(starsNumber: Int) {
    STAR_1(1),
    STAR_2(2),
    STAR_3(3)
}
