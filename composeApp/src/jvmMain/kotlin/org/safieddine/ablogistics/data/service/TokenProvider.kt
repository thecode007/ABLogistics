package org.safieddine.ablogistics.data.service

interface TokenProvider {
    fun currentToken(): String?
    fun currentRefreshToken(): String?
}

