import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.changelog") version "2.2.1"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    // id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}
fun properties(key: String) = project.findProperty(key).toString()

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginVerifierIdeVersions: String by project
val intellijPublishChannel: String by project
val intellijPublishToken: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion
println("version: $version")
// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(pluginName_)
    version.set(platformVersion)
    type.set(platformType)
    downloadSources.set(platformDownloadSources.toBoolean())
    updateSinceUntilBuild.set(true)
    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}
dependencies {
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

// Read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(pluginVersion)
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
}
tasks {
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    publishPlugin {
        token.set(intellijPublishToken)
        channels.set(listOf(intellijPublishChannel, "default").filter(String::isNotEmpty).take(1))
    }
    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
    }

    runPluginVerifier {
        ideVersions.set(
            properties("pluginVerifierIdeVersions")
                .split(',').map(String::trim).filter(String::isNotEmpty)
        )
    }
}
tasks.getByName("buildSearchableOptions").onlyIf { false }