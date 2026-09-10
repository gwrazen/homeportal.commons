group = "pl.homeportal"
version = "8.0"

// Single source of versions, mirroring the <dependencyManagement> of the Maven build.
// Modules declare dependencies without versions; the constraints below supply them.
val slf4jVersion = "1.7.36"
val commonsLang3Version = "3.8.1"
val commonsIoVersion = "2.6"
val velocityVersion = "1.7"
val imgscalrVersion = "4.2"
val commonsEmailVersion = "1.2"

val lombokVersion = "1.18.30"

val javassistVersion = "3.29.2-GA"
val springBootBomVersion = "3.3.5"
val hibernateSearchVersion = "7.1.1.Final"

val jakartaValidationVersion = "3.0.2"
val jakartaServletVersion = "6.0.0"
val jakartaMailVersion = "2.0.1"
val jakartaElVersion = "5.0.0"
val guavaVersion = "27.0.1-jre"
val junitVersion = "4.11"
val hamcrestVersion = "1.3"
val h2Version = "1.4.200"
val jakartaXmlBindVersion = "4.0.2"
val aspectjVersion = "1.9.2"
val jacksonVersion = "2.9.8"


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
        // Jeden BOM Boota zamiast spring-framework-bom + spring-data-releasetrain (ten drugi
        // nie istnieje po Springu 5). Ta sama wersja, na ktorej stoi hac — commons ma byc
        // linia, przeciw ktorej konsumenci na Boocie 3 sie kompiluja.
        add("api", platform("org.springframework.boot:spring-boot-dependencies:$springBootBomVersion"))

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
            add("api", "com.sun.mail:jakarta.mail:$jakartaMailVersion")
            add("api", "jakarta.xml.bind:jakarta.xml.bind-api:$jakartaXmlBindVersion")
            add("api", "org.glassfish.jaxb:jaxb-runtime:$jakartaXmlBindVersion")
            // hibernate-core i validator wersjonuje BOM Boota. hibernate-entitymanager
            // NIE ISTNIEJE w 6.x — jego zawartosc wchlonelo hibernate-core.
            add("api", "org.hibernate.search:hibernate-search-mapper-orm:$hibernateSearchVersion")
            add("api", "org.hibernate.search:hibernate-search-backend-lucene:$hibernateSearchVersion")
            add("api", "jakarta.validation:jakarta.validation-api:$jakartaValidationVersion")
            add("api", "com.google.guava:guava:$guavaVersion")
            add("api", "com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
            add("api", "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
            add("api", "org.aspectj:aspectjrt:$aspectjVersion")
            add("api", "org.aspectj:aspectjweaver:$aspectjVersion")
            add("api", "jakarta.servlet:jakarta.servlet-api:$jakartaServletVersion")
            add("api", "org.glassfish.expressly:expressly:$jakartaElVersion")
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
            // Podglad przed wydaniem: komplet artefaktow ladzie w katalogu na dysku,
            // rejestr pozostaje nietkniety.
            maven {
                name = "staging"
                url = uri(
                    providers.gradleProperty("publishRepoUrl").getOrElse("file:///tmp/commons-staging")
                )
            }
            // Wydanie. Rejestr przyjmuje wersje RAZ — poprawka po wydaniu to kolejny numer.
            // Poswiadczenia ze srodowiska: Gradle nie czyta ~/.m2/settings.xml.
            maven {
                name = "githubPackages"
                url = uri("https://maven.pkg.github.com/gwrazen/homeportal.commons")
                credentials {
                    username = providers.environmentVariable("GITHUB_ACTOR").orNull
                    password = providers.environmentVariable("GITHUB_TOKEN").orNull
                }
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
