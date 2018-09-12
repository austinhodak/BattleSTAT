package com.respondingio.battlegroundsbuddy.models

import java.io.Serializable

data class MatchRoster (
        val type: String,
        val relationships: Relationships,
        val id: String,
        val attributes: RosterAttributes
) : Serializable

data class RosterAttributes (
        val won: Boolean,
        val shardId: String,
        val stats: AttributesStats
)

data class Relationships (
        val participants: Participants,
        val team: Team
)

data class Participants (
        val data: List<Participant>
)

data class Participant (
        val type: String,
        val id: String
)

data class Team (
        val data: String
)

data class AttributesStats (
        val rank: Int,
        val teamId: Int
)