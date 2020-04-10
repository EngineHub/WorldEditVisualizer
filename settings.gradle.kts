rootProject.name = "WorldEditVisualizer"

logger.lifecycle("""
*******************************************
 You are building ${rootProject.name}!

 If you encounter trouble:
 1) Read COMPILING.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/enginehub

 Output files will be in [subproject]/build/libs
*******************************************
""")

listOf("core", "fabric", "forge").forEach {
    include("weviz-$it")
}
