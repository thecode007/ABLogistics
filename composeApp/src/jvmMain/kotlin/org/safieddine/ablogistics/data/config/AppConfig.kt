package org.safieddine.ablogistics.data.config

object AppConfig {
    val baseUrl: String by lazy {
        // Read from JVM system property set in Gradle run/packaging; fallback is safe default
        System.getProperty("app.apiBaseUrl") ?: "http://localhost:3001/api/"
    }
}

