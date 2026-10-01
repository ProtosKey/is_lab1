plugins {
    id("java")
    id("war")
}

group = "dev.protoskey"
version = "1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("jakarta.platform:jakarta.jakartaee-api:11.0.0")
    implementation("org.postgresql:postgresql:42.7.4")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

tasks.war {
    archiveFileName = "is_lab_1.war"
}
