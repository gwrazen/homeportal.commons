dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-domain"))

    api("org.apache.commons:commons-lang3")
    api("org.slf4j:slf4j-api")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-tx")
    // hibernate-entitymanager nie istnieje w 6.x — wchlonelo je hibernate-core.
    api("org.hibernate.orm:hibernate-core")
    api("org.hibernate.search:hibernate-search-mapper-orm")
    api("org.hibernate.search:hibernate-search-backend-lucene")
    api("jakarta.xml.bind:jakarta.xml.bind-api")
    runtimeOnly("org.glassfish.jaxb:jaxb-runtime")

    provided("org.projectlombok:lombok")

    testImplementation("org.junit.jupiter:junit-jupiter")
    // Jawnie: Gradle 8 dokłada launcher sam, Gradle 9 juz nie i test task nie startuje
    // wcale (TestFrameworkNotAvailableException). Ta linia uniezaleznia build od wersji.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.hamcrest:hamcrest-all")
    // In-memory database for the index<->query integration test. Loaded through
    // src/test/resources/META-INF/persistence.xml, not through any import.
    testImplementation("com.h2database:h2")
}
