java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

configurations {
    runtimeElements {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
        }
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT")
}
