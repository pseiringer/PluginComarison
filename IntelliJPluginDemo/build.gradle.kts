plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.example"
version = "1.2-SNAPSHOT"

repositories {
    mavenCentral()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation("com.google.guava:guava:32.1.2-jre")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("233.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("IJ_PluginSign_CertChain"))
        privateKey.set(System.getenv("IJ_PluginSign_PK"))
        // privateKey.set(System.getenv("IJ_PluginSign_PK")) is used for signing in the repository
//        // providers.environmentVariable("IJ_PluginSign_PK_full") is used for local
//        // signing, since the private key gets cropped when saved in windows
//        // environment variables
//        privateKey.set(providers.environmentVariable("IJ_PluginSign_PK_full"))
        password.set(System.getenv("IJ_PluginSign_Pass"))
    }

    publishPlugin {
        token.set(System.getenv("IJ_PluginSign_PublishToken"))
    }
}
