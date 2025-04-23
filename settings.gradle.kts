pluginManagement {
    plugins {
        id("net.neoforged.moddev") version "2.0.49-beta"
        id("net.neoforged.moddev.repositories") version "2.0.49-beta"
        id("com.diffplug.spotless") version "6.25.0"
    }
}

plugins {
    id("net.neoforged.moddev.repositories")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

run {
    @Suppress("UnstableApiUsage")
    dependencyResolutionManagement {
        repositoriesMode = RepositoriesMode.PREFER_SETTINGS
        rulesMode = RulesMode.PREFER_SETTINGS

        repositories {
            mavenCentral()

            maven {
                name = "ModMaven (K4U-NL)"
                url = uri("https://modmaven.dev/")
                content {
                    includeGroup("de.mari_023")
                }
            }

            maven {
                name = "Curse Maven"
                url = uri("https://cursemaven.com")
                content {
                    includeGroup("curse.maven")
                }
            }
        }

        versionCatalogs {
            create("libs") {
                val mc = "1.21.1"
                version("minecraft", mc)

                val nf = mc.substringAfter('.')
                version("neoforge", "${nf + (if (!nf.contains('.')) ".0" else "")}.119")
                version("parchment", "2024.11.17")

                library("ae2", "org.appliedenergistics", "appliedenergistics2").version("19.2.9")
                library("projecte", "curse.maven", "projecte-226410").version("6301953-api-6301954")

                version("ae2wtlib", "19.2.3")
                library("ae2wtlib", "de.mari_023", "ae2wtlib").versionRef("ae2wtlib")
                library("ae2wtlibapi", "de.mari_023", "ae2wtlib_api").versionRef("ae2wtlib")

                library("teampe", "curse.maven", "team-projecte-689273").version("5402805")
            }
        }
    }
}
