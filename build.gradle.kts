plugins {
    java
    checkstyle
    jacoco
    distribution
    id("com.github.spotbugs") version "5.0.5"
    id("com.diffplug.spotless") version "6.2.0"
    id("org.omegat.gradle") version "1.5.7"
    id("com.palantir.git-version") version "0.12.3"
}

// calculate version string from git tag, hash and commit distance
fun getVersionDetails(): com.palantir.gradle.gitversion.VersionDetails = (extra["versionDetails"] as groovy.lang.Closure<*>)() as com.palantir.gradle.gitversion.VersionDetails
if (getVersionDetails().isCleanTag) {
    version = getVersionDetails().lastTag.substring(1)
} else {
    version = getVersionDetails().lastTag.substring(1) + "-" + getVersionDetails().commitDistance + "-" + getVersionDetails().gitHash + "-SNAPSHOT"
}

group = "tokyo.northside"
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    mavenLocal()
}

omegat {
    version = "5.8.0"
    pluginClass = "tokyo.northside.omegat.oxford.OxfordDictionaryPlugin"
}

dependencies {
    packIntoJar("tokyo.northside:java-oxford-dictionaries:0.3.1")
}

spotbugs {
    excludeFilter.set(project.file("config/spotbugs/exclude.xml"))
    tasks.spotbugsMain {
        reports.create("html") {
            required.set(true)
        }
    }
    tasks.spotbugsTest {
        reports.create("html") {
            required.set(true)
        }
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
    options.compilerArgs.add("-Xlint:unchecked")
}

distributions {
    main {
        contents {
            from(tasks["jar"], "README.md", "CHANGELOG.md", "COPYING")
        }
    }
}
