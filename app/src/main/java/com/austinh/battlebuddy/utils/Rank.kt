package com.austinh.battlebuddy.utils

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