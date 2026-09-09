group = "pl.homeportal"
version = "7.1"

// Single source of versions, mirroring the <dependencyManagement> of the Maven build.
// Modules declare dependencies without versions; the constraints below supply them.
val slf4jVersion = "1.7.36"
val commonsLang3Version = "3.8.1"
val commonsIoVersion = "2.6"
val velocityVersion = "1.7"
val imgscalrVersion = "4.2"
val commonsEmailVersion = "1.2"
val javaxMailVersion = "1.5.6"
val lombokVersion = "1.18.30"
val jaxbVersion = "2.3.1"
val javassistVersion = "3.29.2-GA"
val hibernateCoreVersion = "5.0.10.Final"
val hibernateSearchVersion = "5.5.4.Final"
val hibernateValidatorVersion = "6.1.5.Final"
val jakartaValidationVersion = "2.0.2"
val luceneVersion = "5.3.1"
val springBomVersion = "5.2.9.RELEASE"
val springDataReleasetrainVersion = "Moore-SR10"
val guavaVersion = "27.0.1-jre"
val junitVersion = "4.11"
val hamcrestVersion = "1.3"
val h2Version = "1.4.200"
val servletApiVersion = "2.5"
val aspectjVersion = "1.9.2"
val jacksonVersion = "2.9.8"
val elApiVersion = "3.0.0"
val elVersion = "2.2.4"
val commonsCompressVersion = "1.0"
val xercesVersion = "2.12.2"

subprojects {
    apply(plugin = "java-library")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
        withSourcesJar()
    }

    dependencies {
        // BOMs come first: they decide every spring-* and spring-data-* version.
        // Never pin those individually - a spring-core / spring-context drift ends
        // as NoSuchMethodError at the consumer.
        add("api", platform("org.springframework:spring-framework-bom:$springBomVersion"))
        add("api", platform("org.springframework.data:spring-data-releasetrain:$springDataReleasetrainVersion"))

        // Lombok is an annotation processor: two declarations, never on the runtime classpath.
        // Test sources use it too (Maven's 'provided' covered them; Gradle needs its own pair).
        add("compileOnly", "org.projectlombok:lombok:$lombokVersion")
        add("annotationProcessor", "org.projectlombok:lombok:$lombokVersion")
        add("testCompileOnly", "org.projectlombok:lombok:$lombokVersion")
        add("testAnnotationProcessor", "org.projectlombok:lombok:$lombokVersion")

        constraints {
            add("api", "org.slf4j:slf4j-api:$slf4jVersion")
            add("api", "org.apache.commons:commons-lang3:$commonsLang3Version")
            add("api", "org.apache.velocity:velocity:$velocityVersion")
            add("api", "org.imgscalr:imgscalr-lib:$imgscalrVersion")
            add("api", "org.apache.commons:commons-email:$commonsEmailVersion")
            // Must be com.sun.mail:javax.mail, never javax.mail:mail - the latter stopped
            // at 1.4.7 (2013) and negotiates SSLv3/TLSv1, both disabled in newer JDKs.
            add("api", "com.sun.mail:javax.mail:$javaxMailVersion")
            add("api", "javax.xml.bind:jaxb-api:$jaxbVersion")
            add("api", "org.glassfish.jaxb:jaxb-runtime:$jaxbVersion")
            add("api", "org.hibernate:hibernate-core:$hibernateCoreVersion")
            add("api", "org.hibernate:hibernate-entitymanager:$hibernateCoreVersion")
            add("api", "org.hibernate:hibernate-search-orm:$hibernateSearchVersion")
            add("api", "org.hibernate:hibernate-search-engine:$hibernateSearchVersion")
            add("api", "org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
            add("api", "jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
            add("api", "org.apache.lucene:lucene-queryparser:$luceneVersion")
            add("api", "com.google.guava:guava:$guavaVersion")
            add("api", "com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
            add("api", "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
            add("api", "org.aspectj:aspectjrt:$aspectjVersion")
            add("api", "org.aspectj:aspectjweaver:$aspectjVersion")
            add("api", "javax.servlet:servlet-api:$servletApiVersion")
            add("api", "javax.el:javax.el-api:$elApiVersion")
            add("api", "org.glassfish.web:javax.el:$elVersion")
            add("api", "xerces:xercesImpl:$xercesVersion")
            add("api", "junit:junit:$junitVersion")
            add("api", "org.hamcrest:hamcrest-all:$hamcrestVersion")
            add("api", "com.h2database:h2:$h2Version")
            add("api", "org.slf4j:slf4j-simple:$slf4jVersion")

            // Transitive-only overrides. No module declares these; they exist to cover
            // what Hibernate and friends drag in. javassist 3.18 defines classes through
            // ClassLoader.defineClass, which JPMS blocks from Java 16 on.
            add("api", "org.javassist:javassist:$javassistVersion")
            add("api", "commons-io:commons-io:$commonsIoVersion")
            add("api", "org.apache.commons:commons-compress:$commonsCompressVersion")
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnit()
        systemProperty("file.encoding", "UTF-8")
        testLogging {
            events("failed")
        }
    }
}
