dependencies {
    api("org.springframework:spring-web")
    api("org.springframework:spring-context")
    api("org.springframework:spring-webmvc")
    api("jakarta.validation:jakarta.validation-api")
    api("org.slf4j:slf4j-api")
    api("org.imgscalr:imgscalr-lib")
    api("org.hibernate.validator:hibernate-validator")
    api("org.apache.commons:commons-lang3")
    api("com.google.guava:guava")
    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.aspectj:aspectjrt")
    api("org.aspectj:aspectjweaver")

    // jakarta.el has no import anywhere in the code but IS needed at runtime: Hibernate
    // Validator interpolates messages through Expression Language. Without these two
    // ObjectValidator dies on startup (NoClassDefFoundError: javax/el/ELManager).
    // Do not drop them based on an import scan or on dependency:analyze.
    api("org.glassfish.expressly:expressly")
    api("xerces:xercesImpl")

    provided("jakarta.servlet:jakarta.servlet-api")
    provided("org.projectlombok:lombok")

    testImplementation("junit:junit")
}
