plugins {
    java
}

group = "net.azisaba"
version = "1.0.0"

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
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://repo2.acrylicstyle.xyz") }
    }

    dependencies {
        if (name != "common") implementation(project(":common"))
        implementation("net.blueberrymc:native-util:1.2.5")
        implementation("org.javassist:javassist:3.28.0-GA")
        compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
        compileOnly("org.jetbrains:annotations:22.0.0")
    }
}
