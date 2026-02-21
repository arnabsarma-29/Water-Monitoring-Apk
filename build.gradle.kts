// build.gradle.kts (project-level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.1") // match your Android Gradle Plugin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }
}
