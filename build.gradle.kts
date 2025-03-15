plugins {
    idea
    eclipse
    id("net.neoforged.moddev")
    id("com.diffplug.spotless")
}

val modId = "appliede"

base.archivesName = modId
version = System.getenv("APPE_VERSION") ?: "0.0.0"
group = "gripe.90"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

dependencies {
    implementation(libs.ae2)
    implementation(libs.projecte)

    compileOnly(libs.ae2wtlibapi)
    jarJar(libs.ae2wtlibapi)
    runtimeOnly(libs.ae2wtlib)

    compileOnly(libs.teampe)

    runtimeOnly(libs.jade)
}

neoForge {
    version = libs.versions.neoforge.get()

    parchment {
        // minecraftVersion = libs.versions.minecraft.get()
        minecraftVersion = "1.21"
        mappingsVersion = libs.versions.parchment.get()
    }

    mods {
        create(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            gameDirectory = file("run")
        }

        create("client") {
            client()
        }

        create("server") {
            server()
            gameDirectory = file("run/server")
        }
    }
}

tasks {
    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$modId" }
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(props)
        }
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

spotless {
    kotlinGradle {
        target("*.kts")
        diktat()
    }

    java {
        target("/src/**/java/**/*.java")
        endWithNewline()
        indentWithSpaces(4)
        removeUnusedImports()
        palantirJavaFormat()
        importOrderFile(file("mega.importorder"))
        toggleOffOn()

        // courtesy of diffplug/spotless#240
        // https://github.com/diffplug/spotless/issues/240#issuecomment-385206606
        custom("noWildcardImports") {
            if (it.contains("*;\n")) {
                throw Error("No wildcard imports allowed")
            }

            it
        }

        bumpThisNumberIfACustomStepChanges(1)
    }

    json {
        target("src/**/resources/**/*.json")
        targetExclude("src/generated/resources/**")
        biome()
        indentWithSpaces(2)
        endWithNewline()
    }
}
