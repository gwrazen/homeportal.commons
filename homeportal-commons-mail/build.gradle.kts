dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-logging"))

    api("org.slf4j:slf4j-api")
    api("com.sun.mail:jakarta.mail")

    provided("org.apache.commons:commons-email2-jakarta")
    provided("org.apache.velocity:velocity")
    provided("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter")
    // Gradle 9 nie dokłada juz launchera sam - bez tego test task nie startuje wcale.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
