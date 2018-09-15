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
) : Serializable

data class Relationships (
        val participants: Participants,
        val team: Team
) : Serializable

data class Participants (
        val data: List<Participant>
) : Serializable

data class Participant (
        val type: String,
        val id: String
) : Serializable

data class Team (
        val data: String
) : Serializable

data class AttributesStats (
        val rank: Int,
        val teamId: Int
) : Serializable