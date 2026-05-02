package com.disheveled.dailyquotes

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform