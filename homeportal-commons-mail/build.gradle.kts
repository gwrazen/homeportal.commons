dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-logging"))

    api("org.slf4j:slf4j-api")
    api("com.sun.mail:javax.mail")

    // Maven scope 'provided': compile and test classpath, never the consumer's runtime.
    compileOnly("org.apache.commons:commons-email")
    compileOnly("org.apache.velocity:velocity")
    testImplementation("org.apache.commons:commons-email")
    testImplementation("org.apache.velocity:velocity")

    testImplementation("junit:junit")
}
