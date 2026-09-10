dependencies {
    api("org.apache.commons:commons-lang3")

    testImplementation("org.junit.jupiter:junit-jupiter")
    // Gradle 9 nie dokłada juz launchera sam - bez tego test task nie startuje wcale.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.hamcrest:hamcrest-all")
}
