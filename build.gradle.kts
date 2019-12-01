import org.jetbrains.kotlin.gradle.frontend.KotlinFrontendExtension
import org.jetbrains.kotlin.gradle.frontend.npm.NpmExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackExtension
import org.jetbrains.kotlin.gradle.frontend.webpack.WebPackRunTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

buildscript {
  extra.set("production", (findProperty("prod") ?: findProperty("production") ?: "false") == "true")

  dependencies {
    classpath("pl.treksoft:kvision-gradle-plugin:${System.getProperty("kvisionVersion")}")
  }
}

plugins {
  val kotlinVersion: String by System.getProperties()
  id("kotlinx-serialization") version kotlinVersion
  id("kotlin-multiplatform") version kotlinVersion
  id("kotlin-dce-js") version kotlinVersion
  kotlin("frontend") version System.getProperty("frontendPluginVersion")
}

apply(plugin = "pl.treksoft.kvision")

version = "1.0.0-SNAPSHOT"
group = "com.hwboard"

repositories {
  mavenCentral()
  jcenter()
  maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
  maven { url = uri("https://kotlin.bintray.com/kotlinx") }
  maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
  maven { url = uri("https://dl.bintray.com/gbaldeck/kotlin") }
  maven { url = uri("https://dl.bintray.com/rjaros/kotlin") }
  maven { url = uri("https://jitpack.io") }
  mavenLocal()
}

// Versions
val kotlinVersion: String by System.getProperties()
val ktorVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val h2Version: String by project
val pgsqlVersion: String by project
val kweryVersion: String by project
val logbackVersion: String by project
val kvisionVersion: String by System.getProperties()
val oauthVersion: String by project
val fuelVersion: String by project
val commonsCodecVersion: String by project
val jdbcNamedParametersVersion: String by project

// Custom Properties
val webDir = file("src/frontendMain/web")
val isProductionBuild = project.extra.get("production") as Boolean
val mainClassName = "io.ktor.server.netty.EngineMain"

kotlin {
  jvm("backend") {
    compilations.all {
      kotlinOptions {
        jvmTarget = "1.8"
      }
    }
  }
  js("frontend") {
    compilations.all {
      kotlinOptions {
        moduleKind = "umd"
        sourceMap = !isProductionBuild
        metaInfo = true
        if (!isProductionBuild) {
          sourceMapEmbedSources = "always"
        }
      }
    }
  }
  sourceSets {
    getByName("commonMain") {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation("pl.treksoft:kvision-common-types:$kvisionVersion")
      }
    }
    getByName("backendMain") {
      dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("pl.treksoft:kvision-server-ktor:$kvisionVersion")
        implementation("io.ktor:ktor-server-netty:$ktorVersion")
        implementation("ch.qos.logback:logback-classic:$logbackVersion")
        implementation("com.github.mazine:oauth2-client-kotlin:$oauthVersion")
        implementation("org.glassfish.jersey.media:jersey-media-json-jackson:2.22")
        implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
        implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
      }
    }
    getByName("backendTest") {
      dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-junit"))
      }
    }
    getByName("frontendMain") {
      resources.srcDir(webDir)
      dependencies {
        implementation(kotlin("stdlib-js"))
        implementation("pl.treksoft:kvision:$kvisionVersion")
        implementation("pl.treksoft:kvision-bootstrap:$kvisionVersion")
        implementation("pl.treksoft:kvision-fontawesome:$kvisionVersion")
        implementation("pl.treksoft:kvision-remote:$kvisionVersion")
        implementation("pl.treksoft:kvision-bootstrap-css:$kvisionVersion")
        implementation("pl.treksoft:kvision-chart:$kvisionVersion")
      }

    }
    getByName("frontendTest") {
      dependencies {
        implementation(kotlin("test-js"))
      }
    }
  }
}

ktor {
  port = 8080
  mainClass = mainClassName
  jvmOptions = arrayOf()
  workDir = buildDir
}

kotlinFrontend {
  sourceMaps = !isProductionBuild
  webpackBundle {
    bundleName = "main"
    sourceMapEnabled = false
    port = 3000
    proxyUrl = "http://localhost:${ktor.port}"
    contentPath = webDir
    mode = if (isProductionBuild) "production" else "development"
  }

  define("PRODUCTION", isProductionBuild)
}

tasks {
  withType<Kotlin2JsCompile> {
    kotlinOptions {
      moduleKind = "umd"
      sourceMap = !isProductionBuild
      metaInfo = true
      if (!isProductionBuild) {
        sourceMapEmbedSources = "always"
      }
    }
  }
  withType<KotlinJsDce> {
    dceOptions {
      devMode = !isProductionBuild
    }
    inputs.property("production", isProductionBuild)
    doFirst {
      destinationDir.deleteRecursively()
    }
    doLast {
      copy {
        file("$buildDir/node_modules_imported/").listFiles()?.forEach {
          if (it.isDirectory && it.name.startsWith("kvision")) {
            from(it) {
              include("css/**")
              include("img/**")
              include("js/**")
            }
          }
        }
        into(file(buildDir.path + "/kotlin-js-min/frontend/main"))
      }
    }
  }
}
afterEvaluate {
  tasks {
    getByName("webpack-run", WebPackRunTask::class) {
      dependsOn("frontendMainClasses")
    }
    getByName("webpack-bundle") {
      dependsOn("frontendMainClasses", "runDceFrontendKotlin")
      doFirst {
        copy {
          from((project.tasks["frontendProcessResources"] as Copy).destinationDir)
          into((project.tasks["processResources"] as Copy).destinationDir)
        }
      }
    }
    replace("frontendJar", Jar::class).apply {
      dependsOn("webpack-bundle")
      group = "package"
      archiveAppendix.set("frontend")
      val from = project.tasks["webpack-bundle"].outputs.files + webDir
      from(from)
      into("/assets")
      inputs.files(from)
      outputs.file(archiveFile)

      manifest {
        attributes(
            mapOf(
                "Implementation-Title" to rootProject.name,
                "Implementation-Group" to rootProject.group,
                "Implementation-Version" to rootProject.version,
                "Timestamp" to System.currentTimeMillis()
            )
        )
      }
    }
    create("frontendZip", Zip::class) {
      dependsOn("webpack-bundle")
      group = "package"
      archiveAppendix.set("frontend")
      destinationDirectory.set(file("$buildDir/libs"))
      val from = project.tasks["webpack-bundle"].outputs.files + webDir
      from(from)
      inputs.files(from)
      outputs.file(archiveFile)
    }
    create("restart") {
      group = "run"
      dependsOn("webpack-bundle", "ktor-run", "ktor-stop")
      findByName("ktor-run")?.mustRunAfter("ktor-stop")
      findByName("ktor-run")?.mustRunAfter("webpack-bundle")
    }
    getByName("backendJar").group = "package"
    replace("jar", Jar::class).apply {
      dependsOn("frontendJar", "backendJar")
      group = "package"
      manifest {
        attributes(
            mapOf(
                "Implementation-Title" to rootProject.name,
                "Implementation-Group" to rootProject.group,
                "Implementation-Version" to rootProject.version,
                "Timestamp" to System.currentTimeMillis(),
                "Main-Class" to mainClassName
            )
        )
      }
      val dependencies = configurations["backendRuntimeClasspath"].filter { it.name.endsWith(".jar") } +
          project.tasks["backendJar"].outputs.files +
          project.tasks["frontendJar"].outputs.files
      dependencies.forEach {
        if (it.isDirectory) from(it) else from(zipTree(it))
      }
      exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
      inputs.files(dependencies)
      outputs.file(archiveFile)
    }
    create("frontendRun") {
      dependsOn("webpack-run")
      group = "run"
    }
    create("backendRun") {
      dependsOn("ktor-run")
      group = "run"
    }
    getByName("run") {
      dependsOn("frontendRun", "backendRun")
    }
    create("frontendStop") {
      dependsOn("webpack-stop")
      group = "run"
    }
    create("backendStop") {
      dependsOn("ktor-stop")
      group = "run"
    }
    getByName("stop") {
      dependsOn("frontendStop", "backendStop")
    }
    getByName("compileKotlinBackend") {
      dependsOn("compileKotlinMetadata")
    }
    getByName("compileKotlinFrontend") {
      dependsOn("compileKotlinMetadata")
    }
  }
}

fun KotlinFrontendExtension.webpackBundle(block: WebPackExtension.() -> Unit) =
    bundle("webpack", delegateClosureOf(block))

fun KotlinFrontendExtension.npm(block: NpmExtension.() -> Unit) = configure(block)
