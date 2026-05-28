package com.disheveled.dailyquotes.data.api

import io.ktor.client.engine.HttpClientEngine

expect fun platformHttpClientEngine(): HttpClientEngine
