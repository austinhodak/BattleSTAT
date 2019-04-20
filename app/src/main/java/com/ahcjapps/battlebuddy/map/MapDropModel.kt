package com.ahcjapps.battlebuddy.map

data class MapDropModel (
        var name: String,
        var logo: String,
        var mapVector: String,
        var zones: List<MapZone>
)

data class MapZone (
        var name: String,
        var weight: Double,
        var x: Int,
        var y: Int
)