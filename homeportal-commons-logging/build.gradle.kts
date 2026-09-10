dependencies {
    api(project(":homeportal-commons-java"))

    // Deliberately -domain, not -data: LoggingSupport needs only the Identifiable
    // contract. Depending on -data dragged Hibernate, Hibernate Search and Lucene
    // in here, and transitively into -mail.
    api(project(":homeportal-commons-domain"))

    testImplementation("org.junit.jupiter:junit-jupiter")
    // Gradle 9 nie dokłada juz launchera sam - bez tego test task nie startuje wcale.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Test-only slf4j binding: without it LoggerFactory returns NOPLogger and the
    // logging layer cannot be tested. The consumer supplies the production binding.
    testImplementation("org.slf4j:slf4j-simple")
}
