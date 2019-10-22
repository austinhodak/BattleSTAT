package com.ahcjapps.battlebuddy.models

data class Asset(val id: String, val attributes: AssetAttributes) {
    data class AssetAttributes(val URL: String)
}