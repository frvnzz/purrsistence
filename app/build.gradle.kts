import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    jacoco
}

jacoco {
    toolVersion = "0.8.14"
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun localProperty(name: String): String {
    return localProperties.getProperty(name)
        ?: project.findProperty(name) as? String
        ?: ""
}

android {
    namespace = "com.example.purrsistence"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.purrsistence"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProperty("SUPABASE_URL")}\""
        )

        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            "\"${localProperty("SUPABASE_PUBLISHABLE_KEY")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.extensions.configure(JacocoTaskExtension::class.java) {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }
            }
        }
    }
}

val jacocoExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "android/**/*.*",
    "**/*Test*.*",
    $$"**/*$Lambda$*.*",
    $$"**/*$inlined$*.*",
    "**/*_Factory*.*",
    "**/*_Provide*Factory*.*",
    "**/*_MembersInjector*.*",
    "**/*_Impl*.*",
    "**/*_ImplKt*.*",
    "**/*Dao_Impl*.*",
    "**/*JsonAdapter*.*",
    "**/*MapperImpl*.*",
    "**/*ComposableSingletons*.*",
    "**/*Preview*.*",
    $$"**/*$serializer*.*",
    "**/hilt_aggregated_deps/**",
    "**/databinding/**",
    "**/generated/**",
    "**/testFixtures/**"
)

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates JaCoCo XML and HTML coverage reports for debug unit tests."
    dependsOn("testDebugUnitTest")

    val javaClassesDir =
        layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")
    val kotlinClassesDir = layout.buildDirectory.dir("tmp/kotlin-classes/debug")
    val kotlinBuiltInClassesDir =
        layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")

    reports {
        xml.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        )
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(
        files(
            fileTree(javaClassesDir) {
                exclude(jacocoExcludes)
            },
            fileTree(kotlinClassesDir) {
                exclude(jacocoExcludes)
            },
            fileTree(kotlinBuiltInClassesDir) {
                exclude(jacocoExcludes)
            }
        )
    )

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))

    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec",
                "jacoco/test.exec"
            )
        }
    )
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    group = "verification"
    description = "Verifies debug unit test coverage against configured JaCoCo thresholds."
    dependsOn("jacocoTestReport")

    classDirectories.setFrom(
        tasks.named("jacocoTestReport", JacocoReport::class.java).map { it.classDirectories })
    sourceDirectories.setFrom(
        tasks.named("jacocoTestReport", JacocoReport::class.java).map { it.sourceDirectories })
    executionData.setFrom(
        tasks.named("jacocoTestReport", JacocoReport::class.java).map { it.executionData })

    violationRules {
        rule {
            enabled = true
            element = "BUNDLE"

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.30".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.20".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoTestCoverageVerification")
}

dependencies {
    // ─────────── Core / AndroidX ───────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.activity.compose)

    // ─────────── Compose ───────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material3)

    // ─────────── Room ───────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ─────────── Unit tests ───────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // ─────────── Android tests ───────────
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // ─────────── Debug ───────────
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    val room_version = "2.8.4"

    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    //Vico Compose Graphs
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.coil.compose)

    //--------------- Supabase --------------
    implementation(platform("io.github.jan-tennert.supabase:bom:3.6.0"))

    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.ktor:ktor-client-okhttp:3.3.0")
}