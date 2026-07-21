plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.gtone.sqlplugin"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        local("C:/Program Files/JetBrains/IntelliJ IDEA 2026.1.2")
        bundledPlugin("com.intellij.database")
    }
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261"
            untilBuild = "261.*"
        }
    }
}
