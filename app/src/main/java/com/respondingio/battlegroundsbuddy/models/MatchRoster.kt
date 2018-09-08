package com.respondingio.battlegroundsbuddy.models

data class MatchRoster(
        val type: String,
        val relationships: Relationships,
        val id: String,
        val attributes: RosterAttributes
)

data class RosterAttributes(
    val won: Boolean,
    val shardId: String
)

data class Relationships(
        val participants: Participants
)

data class Participants(
        val data: List<Participant>
)

data class Participant(
        val type: String,
        val id: String
)

data class Team(
        val data: String
)