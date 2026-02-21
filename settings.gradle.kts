// 1. pluginManagement MUST be first
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
}

// 2. plugins block comes SECOND
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// 3. Everything else comes after
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Water Reminder"
include(":app")