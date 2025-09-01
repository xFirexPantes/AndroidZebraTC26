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
        //jcenter()
        maven {
            url = uri("https://zebratech.jfrog.io/artifactory/EMDK-Android/")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        maven {
            url = uri("https://zebratech.jfrog.io/artifactory/EMDK-Android/")
        }
    }
}

rootProject.name = "scanner"
include(":app")
