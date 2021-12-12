plugins {
    java
}

group = "net.azisaba"
version = "1.0.1"

repositories {
    mavenCentral()
}

subprojects {
    group = parent!!.group
    version = parent!!.version

    apply {
        plugin("java")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://repo2.acrylicstyle.xyz") }
    }

    dependencies {
        if (name != "common") implementation(project(":common"))
        implementation("net.blueberrymc:native-util:1.2.5")
        compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains:annotations:22.0.0")
        compileOnly("io.netty:netty-all:4.1.68.Final")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
        testImplementation("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}
