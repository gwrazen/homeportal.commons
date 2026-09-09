dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-domain"))

    api("org.apache.commons:commons-lang3")
    api("org.slf4j:slf4j-api")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-tx")
    api("org.hibernate:hibernate-core")
    api("org.hibernate:hibernate-entitymanager")
    api("org.hibernate:hibernate-search-orm")
    api("org.hibernate:hibernate-search-engine")
    api("org.apache.lucene:lucene-queryparser")
    api("javax.xml.bind:jaxb-api")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime")

    testImplementation("junit:junit")
    testImplementation("org.hamcrest:hamcrest-all")
    // In-memory database for the index<->query integration test. Loaded through
    // src/test/resources/META-INF/persistence.xml, not through any import.
    testImplementation("com.h2database:h2")
}
