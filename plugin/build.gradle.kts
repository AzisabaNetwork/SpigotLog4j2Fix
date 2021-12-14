plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

dependencies {
    implementation(project(":v1_17"))
    implementation(project(":v1_16_R3"))
    implementation(project(":v1_15_R1"))
    implementation(project(":v1_14_R1"))
    implementation(project(":v1_13_R2"))
    implementation(project(":v1_12_R1"))
    implementation(project(":v1_11_R1"))
    implementation(project(":v1_10_R1"))
    implementation(project(":v1_9_R2"))
    implementation(project(":v1_8_R3"))
}

tasks {
    withType<ProcessResources> {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(parent!!.projectDir) { include("LICENSE") }
    }

    shadowJar {
        relocate("javassist", "net.azisaba.spigotLog4j2Fix.libs.javassist")
        archiveFileName.set("SpigotLog4j2Fix-${project.version}.jar")
    }
}
