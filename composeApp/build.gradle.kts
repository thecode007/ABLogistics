import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.2.0"

}

kotlin {
    jvm()

    sourceSets {
        // Use default hierarchy template; no custom intermediate desktop source set.

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            // Country picker (Compose) – migrated usage
            implementation("network.chaintech:cmp-country-code-picker:1.0.1")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

            implementation("io.github.compose-fluent:fluent:v0.1.0")
            implementation("io.github.compose-fluent:fluent-icons-extended:v0.1.0")

            implementation("io.ktor:ktor-client-core:3.3.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.1")
            implementation("io.ktor:ktor-client-logging:3.3.1")
            implementation("io.ktor:ktor-client-plugins:3.1.1")

            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

            // Required by Material3 DatePicker (used on desktop)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // (Removed external dialog dependency; using custom overlay)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.ktor:ktor-client-cio:3.3.1")
            // Ensure kotlinx-datetime is on JVM runtime classpath (Material3 DatePicker)

        }
    }
}


compose.desktop {
    application {
        mainClass = "org.safieddine.ablogistics.MainKt"
        jvmArgs += listOf("-Dfile.encoding=UTF-8")

        val apiBaseUrl = project.findProperty("app.apiBaseUrl")?.toString()
            ?: "http://localhost:8080/api/"
        jvmArgs += listOf("-Dapp.apiBaseUrl=${apiBaseUrl}")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ABLogistics"
            packageVersion = "2.0.1"
            description = "ABLogistics Application"
            vendor = "Abdallah Safieddine"
            includeAllModules = true

            windows {
                menuGroup = "ABLogistics"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567891" // Generate your own unique UUID
            }
        }
    }
}
tasks.withType<Jar>().configureEach {
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    manifest {
        attributes["Main-Class"] = "org.safieddine.ablogistics.MainKt"
    }
}
