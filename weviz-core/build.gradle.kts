plugins {
    `java-library`
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
}

applyPlatformAndCoreConfiguration()

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "api"("com.sk89q.worldedit:worldedit-core:${Versions.WORLD_EDIT}")
}
