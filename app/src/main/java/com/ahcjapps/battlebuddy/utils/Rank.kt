package com.ahcjapps.battlebuddy.utils

enum class Rank (var title: String, var order: Int) {
    UNKNOWN("Level Not Available", 0),
    BEGINNER("Beginner", 1),
    NOVICE("Novice", 2),
    EXPERIENCED("Experienced", 3),
    SKILLED("Skilled", 4),
    SPECIALIST("Specialist", 5),
    EXPERT("Expert", 6),
    SURVIVOR("Survivor", 7),
    LONE_SURVIVOR("Lone Survivor", 8)
}

enum class RankLevel (var order: Int) {
    `0`(6),
    `1`(5),
    `2`(4),
    `3`(3),
    `4`(2),
    `5`(1)
}