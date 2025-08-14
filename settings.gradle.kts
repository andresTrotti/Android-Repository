pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" apply false
    }
}
// Apply the settings plugin here
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SnapSafe"
include(":app")
 