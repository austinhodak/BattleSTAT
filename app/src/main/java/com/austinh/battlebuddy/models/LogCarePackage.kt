package com.austinh.battlebuddy.models

data class LogCarePackage (
        val itemPackageId: String,
        val location: LogLocation,
        val items: List<LogItem>
)

data class LogCarePackageLand (
        val itemPackage: LogCarePackage
)

data class LogCarePackageSpawn (
        val itemPackage: LogCarePackage
)