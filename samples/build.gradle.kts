/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

plugins {
    java
    application
    id("com.diffplug.spotless") version "6.23.3"
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":java-client"))
    testImplementation(project(":java-client"))
    testAnnotationProcessor(project(":java-client"))
    implementation("org.apache.logging.log4j", "log4j-api","[2.17.1,3.0)")
    implementation("org.apache.logging.log4j", "log4j-core","[2.17.1,3.0)")
    implementation("org.apache.logging.log4j", "log4j-slf4j2-impl","[2.17.1,3.0)")
    implementation("commons-logging", "commons-logging", "1.2")
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.15.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")

    testImplementation("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("commons-io:commons-io:2.15.1")
    testAnnotationProcessor("commons-io:commons-io:2.15.1")
}

spotless {
  java {

    target("**/*.java")

    // Use the default importOrder configuration
    importOrder()
    removeUnusedImports()

    eclipse().configFile("../buildSrc/formatterConfig.xml")

    trimTrailingWhitespace()
    endWithNewline()
  }
}

application {
    mainClass.set("org.opensearch.client.samples.Main")
}

tasks.named<JavaExec>("run") {
    systemProperty("samples.mainClass", System.getProperty("samples.mainClass"))
}

tasks.test {
    useJUnitPlatform()
}
