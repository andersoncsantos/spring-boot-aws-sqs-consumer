import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//plugins {
//	kotlin("jvm") version "1.9.25"
//	kotlin("plugin.spring") version "1.9.25"
//	id("org.springframework.boot") version "3.3.3"
//	id("io.spring.dependency-management") version "1.1.6"
//}

plugins {
	id("org.springframework.boot") version "2.7.17"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

//group = "com.example"
//version = "0.0.1-SNAPSHOT"
//
//java {
//	toolchain {
//		languageVersion = JavaLanguageVersion.of(17)
//	}
//}

repositories {
	mavenCentral()
}

//dependencies {
//	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("org.jetbrains.kotlin:kotlin-reflect")
//	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//	implementation("com.amazonaws:aws-java-sdk-sqs:1.12.200")
//	implementation("org.springframework.cloud:spring-cloud-aws-messaging:2.2.6.RELEASE")
//	testImplementation("org.springframework.boot:spring-boot-starter-test")
//}

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

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}


//kotlin {
//	compilerOptions {
//		freeCompilerArgs.addAll("-Xjsr305=strict")
//	}
//}
//
//tasks.withType<Test> {
//	useJUnitPlatform()
//}
