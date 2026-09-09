dependencies {
    api(project(":homeportal-commons-java"))
    api(project(":homeportal-commons-logging"))

    api("org.slf4j:slf4j-api")
    api("com.sun.mail:javax.mail")

    provided("org.apache.commons:commons-email")
    provided("org.apache.velocity:velocity")
    provided("org.projectlombok:lombok")

    testImplementation("junit:junit")
}
