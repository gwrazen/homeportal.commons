dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-logging"))

    api("org.slf4j:slf4j-api")
    api("com.sun.mail:jakarta.mail")

    provided("org.apache.commons:commons-email2-jakarta")
    provided("org.apache.velocity:velocity")
    provided("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter")
    // Jawnie: Gradle 8 dokłada launcher sam, Gradle 9 juz nie i test task nie startuje
    // wcale (TestFrameworkNotAvailableException). Ta linia uniezaleznia build od wersji.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
