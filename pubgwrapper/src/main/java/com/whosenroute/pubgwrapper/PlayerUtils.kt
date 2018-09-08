package com.whosenroute.pubgwrapper

class PlayerUtils {

    fun runAsync(callback: (test: String, test2: String) -> Unit){
        var test = "TEST"
        callback(test, test)
    }
}