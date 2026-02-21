import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	jacoco
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// AWS SDK v2
	implementation(platform("software.amazon.awssdk:bom:2.20.79"))
	implementation("software.amazon.awssdk:sqs")

	// Spring Cloud AWS
	implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs:3.0.1")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// JaCoCo Configuration
jacoco {
	toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		xml.required.set(true)
		html.required.set(true)
		csv.required.set(false)
	}

	classDirectories.setFrom(files(classDirectories.files.map {
		fileTree(it) {
			exclude(
				"**/config/**",
				"**/entity/**",
				"**/dto/**"
			)
		}
	}))
}

// Generate JaCoCo report after tests
tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}
