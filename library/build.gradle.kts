import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.vanniktech.maven.publish")
    id("signing")
    jacoco
}

android {
    namespace = "com.xrayradar.android"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

jacoco {
    toolVersion = "0.8.16"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
    )
    // AGP 9 built-in Kotlin compiles to intermediates/built_in_kotlinc (was tmp/kotlin-classes)
    val kotlinClasses = fileTree("${layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(fileFilter)
    }
    val javaClasses = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug") {
        exclude(fileFilter)
    }
    classDirectories.setFrom(files(kotlinClasses, javaClasses))

    sourceDirectories.setFrom(
        files(
            "${project.projectDir}/src/main/java",
        ),
    )
    executionData.setFrom(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"))
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    implementation("com.squareup.okhttp3:okhttp:5.5.1")

    implementation("androidx.work:work-runtime-ktx:2.12.0")
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.5.1")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    coordinates(project.group.toString(), "xrayradar-android", project.version.toString())

    pom {
        name.set("XrayRadar Android SDK")
        description.set("Android SDK for sending crash and error telemetry to XrayRadar.")
        url.set("https://rhymesolutions.com/work/xrayradar")
        organization {
            name.set("Rhyme Solutions")
            url.set("https://rhymesolutions.com")
        }
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
            }
        }
        developers {
            developer {
                id.set("kingpegasus")
                name.set("KingPegasus")
                organization.set("Rhyme Solutions")
                organizationUrl.set("https://rhymesolutions.com")
            }
        }
        scm {
            url.set("https://github.com/KingPegasus/XrayRadar-Android-SDK")
            connection.set("scm:git:https://github.com/KingPegasus/XrayRadar-Android-SDK.git")
            developerConnection.set("scm:git:ssh://git@github.com/KingPegasus/XrayRadar-Android-SDK.git")
        }
    }
}

// Sign only when keys are present (CI); skip signing for publishToMavenLocal / local builds
afterEvaluate {
    val signingKey = project.findProperty("ORG_GRADLE_PROJECT_signingInMemoryKey") as String? ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")
    val signingPassword = project.findProperty("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword") as String? ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        extensions.configure<SigningExtension> {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
    } else {
        tasks.withType<Sign>().configureEach {
            enabled = false
        }
    }
}
