plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
}

group = "bayern.kickner"
version = "1.1"

repositories {
    mavenCentral()
}

dependencies {

}



kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

ktor {
    fatJar {
        archiveFileName.set("folder_merger_$version.jar")
    }
}