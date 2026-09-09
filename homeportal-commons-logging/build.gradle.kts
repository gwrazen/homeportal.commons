dependencies {
    api(project(":homeportal-commons-java"))

    // Deliberately -domain, not -data: LoggingSupport needs only the Identifiable
    // contract. Depending on -data dragged Hibernate, Hibernate Search and Lucene
    // in here, and transitively into -mail.
    api(project(":homeportal-commons-domain"))

    testImplementation("junit:junit")
    // Test-only slf4j binding: without it LoggerFactory returns NOPLogger and the
    // logging layer cannot be tested. The consumer supplies the production binding.
    testImplementation("org.slf4j:slf4j-simple")
}
