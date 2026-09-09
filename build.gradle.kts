group = "pl.homeportal"
version = "7.0"

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

    // Maven's 'provided' has no Gradle equivalent: compileOnly is not published at all.
    // This configuration carries those dependencies onto the compile and test classpaths
    // AND into the published pom with an explicit scope (see pom.withXml below).
    // The scope must be explicit here: today it is inherited from the parent pom's
    // dependencyManagement, and a flat pom without it would mean 'compile'.
    val provided = configurations.create("provided")
    configurations.named("compileOnly") { extendsFrom(provided) }
    configurations.named("testImplementation") { extendsFrom(provided) }

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

        // Lombok runs as an annotation processor everywhere; the modules that actually
        // use it also declare it in the 'provided' configuration, so it reaches the pom.
        // Test sources need their own pair - Maven's 'provided' covered both at once.
        add("annotationProcessor", "org.projectlombok:lombok:$lombokVersion")
        add("testCompileOnly", "org.projectlombok:lombok:$lombokVersion")
        add("testAnnotationProcessor", "org.projectlombok:lombok:$lombokVersion")

        constraints {
            add("api", "org.projectlombok:lombok:$lombokVersion")
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

    apply(plugin = "maven-publish")

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom.withXml {
                    val providedDeps = provided.allDependencies.filterIsInstance<ExternalDependency>()
                    if (providedDeps.isNotEmpty()) {
                        // Versions come from the compile classpath: the modules declare these
                        // without one, exactly as the poms do.
                        val resolved = configurations.getByName("compileClasspath")
                            .incoming.resolutionResult.allComponents
                            .mapNotNull { it.moduleVersion }
                            .associate { "${it.group}:${it.name}" to it.version }
                        val root = asNode()
                        val depsNode = (root.get("dependencies") as groovy.util.NodeList)
                            .filterIsInstance<groovy.util.Node>()
                            .firstOrNull() ?: root.appendNode("dependencies")
                        providedDeps.forEach { d ->
                            val key = "${d.group}:${d.name}"
                            val resolvedVersion = d.version
                                ?: resolved[key]
                                ?: error("No resolved version for provided dependency $key in ${project.name}")
                            val node = depsNode.appendNode("dependency")
                            node.appendNode("groupId", d.group)
                            node.appendNode("artifactId", d.name)
                            node.appendNode("version", resolvedVersion)
                            node.appendNode("scope", "provided")
                        }
                    }
                }
            }
        }
        repositories {
            maven {
                name = "staging"
                url = uri(
                    providers.gradleProperty("publishRepoUrl").getOrElse("file:///tmp/commons-staging")
                )
            }
        }
    }

    // Consumers are all Maven. A .module file would win over the pom for a Gradle
    // consumer, so the registry gets exactly what it gets today: jar, sources, pom.
    tasks.withType<GenerateModuleMetadata>().configureEach {
        enabled = false
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
