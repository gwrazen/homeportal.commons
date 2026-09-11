dependencies {
    api("org.apache.commons:commons-lang3")

    testImplementation("org.junit.jupiter:junit-jupiter")
    // Jawnie: Gradle 8 dokłada launcher sam, Gradle 9 juz nie i test task nie startuje
    // wcale (TestFrameworkNotAvailableException). Ta linia uniezaleznia build od wersji.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.hamcrest:hamcrest-all")
}
