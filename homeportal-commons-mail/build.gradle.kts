dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-logging"))

    api("org.slf4j:slf4j-api")
    api("com.sun.mail:jakarta.mail")

    provided("org.apache.commons:commons-email2-jakarta")
    provided("org.apache.velocity:velocity")
    provided("org.projectlombok:lombok")

    testImplementation("junit:junit")
}
