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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://devrepo.kakao.com/nexus/content/groups/public/") }
        // Unity as a Library (Forest) — 사전 빌드된 AAR을 로컬 Maven 저장소로 제공.
        // 자세한 내용은 android/app/src/main/java/com/example/todowithspirits/feature/forest 참고.
        maven { url = uri("$rootDir/vendor/forest-unity/maven") }
    }
}

rootProject.name = "TodoWithSpirits"
include(":app")
include(":data")
include(":domain")
include(":core")
