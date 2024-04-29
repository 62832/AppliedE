pluginManagement {
    repositories {
        maven { url = uri("https://maven.neoforged.net/") }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("neogradle", "net.neoforged.gradle").version("6.0.21")
            plugin("mixin", "org.spongepowered.mixin").version("0.7.+")
            plugin("spotless", "com.diffplug.spotless").version("6.23.3")

            library("forge", "net.neoforged", "forge").version("1.20.1-47.1.54")

            version("ae2", "15.1.0")
            library("ae2", "appeng", "appliedenergistics2-forge").versionRef("ae2")

            version("projecte", "1.0.1")
            library("projecte", "curse.maven", "projecte-226410").version("4901949-api-4901951")

            library("projectex", "curse.maven", "project-expansion-579177").version("5232445")
            library("teampe", "curse.maven", "team-projecte-689273").version("4882959")
            library("jade", "curse.maven", "jade-324717").version("5072729")
        }
    }
}

rootProject.name = "AppliedE"
