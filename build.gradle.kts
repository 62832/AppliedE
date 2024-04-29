plugins {
    alias(libs.plugins.neogradle)
    alias(libs.plugins.mixin)
    alias(libs.plugins.spotless)
}

val modId = "appliede"

base.archivesName = modId
version = System.getenv("APPE_VERSION") ?: "0.0.0"
group = "gripe.90"

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

repositories {
    mavenLocal()
    mavenCentral()

    maven {
        name = "ModMaven (K4U-NL)"
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
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

minecraft {
    mappings("official", "1.20.1")
    copyIdeResources.set(true)
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        configureEach {
            workingDirectory(file("run"))
            property("forge.logging.console.level", "debug")

            mods {
                create(modId) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("client")
        create("server") { workingDirectory(file("run/server")) }
    }
}

dependencies {
    minecraft(libs.forge)
    implementation(fg.deobf(libs.ae2.get()))
    implementation(fg.deobf(libs.projecte.get()))

    runtimeOnly(fg.deobf(libs.projectex.get()))
    runtimeOnly(fg.deobf(libs.teampe.get()))
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
        val props = mapOf(
            "version" to version,
            "ae2Version" to libs.versions.ae2.get(),
            "ae2VersionEnd" to libs.versions.ae2.get().substringBefore('.').toInt() + 1,
            "projectEVersion" to libs.versions.projecte.get(),
        )

        inputs.properties(props)
        filesMatching("META-INF/mods.toml") {
            expand(props)
        }
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
