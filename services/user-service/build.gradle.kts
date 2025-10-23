plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Spring Boot starters
    implementation(libs.bundles.spring.web)
    implementation(libs.bundles.spring.data)
    implementation(libs.bundles.monitoring)
    
    // Database
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2database)
    
    // Cloud and resilience
    implementation(libs.spring.cloud.kubernetes.client)
    implementation(libs.resilience4j.spring)
    
    // MapStruct
    implementation(libs.mapstruct)
    annotationProcessor(libs.mapstruct.processor)
    
    // Testing
    testImplementation(libs.testcontainers.postgresql)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springCloud.get()}")
    }
}

tasks.bootJar {
    archiveFileName.set("user-service.jar")
}
