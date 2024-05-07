pluginManagement {
    repositories {
        maven { url = uri("https://maven.neoforged.net/") }
        maven { url = uri("https://maven.parchmentmc.org") }
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            plugin("neogradle", "net.neoforged.gradle").version("6.0.21")
            plugin("mixin", "org.spongepowered.mixin").version("0.7.+")
            plugin("parchment", "org.parchmentmc.librarian.forgegradle").version("1.+")
            plugin("spotless", "com.diffplug.spotless").version("6.23.3")

            val minecraftVersion = "1.20.1"

            library("forge", "net.neoforged", "forge").version("$minecraftVersion-47.1.54")
            library("mixin", "org.spongepowered", "mixin").version("0.8.5")

            library("ae2", "appeng", "appliedenergistics2-forge").version("15.1.0")
            library("projecte", "curse.maven", "projecte-226410").version("4901949-api-4901951")

            library("teampe", "curse.maven", "team-projecte-689273").version("5313878")
            library("ae2wtlib", "curse.maven", "applied-energistics-2-wireless-terminals-459929").version("5217955")
            library("aecapfix", "curse.maven", "aecapfix-914685").version("5017517")

            library("curios", "top.theillusivec4.curios", "curios-forge").version("5.9.0+$minecraftVersion")
            library("cloth", "me.shedaniel.cloth", "cloth-config-forge").version("11.1.106")
            library("architectury", "dev.architectury", "architectury-forge").version("9.1.12")

            library("projectex", "curse.maven", "project-expansion-579177").version("5232445")
            library("jade", "curse.maven", "jade-324717").version("5072729")
            library("jei", "mezz.jei", "jei-1.20.1-forge").version("15.3.0.4")
        }
    }
}

rootProject.name = "AppliedE"
