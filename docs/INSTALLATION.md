# Installation Guide

This guide provides instructions on how to set up `code-extractor-java` for development or include it as a dependency in your project.

## Prerequisites

- **Java Development Kit (JDK) 17** or higher.
- **Gradle 8.x** (optional, the project includes a Gradle wrapper).

## Building from Source

To build the project and generate the JAR file:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-repo/code-extractor-java.git
   cd code-extractor-java
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Locate the JAR:**
   After a successful build, the JAR file will be located at:
   `build/libs/code-extractor-java-1.0-SNAPSHOT.jar`

## Including in a Gradle Project

If you want to use `code-extractor-java` in another Gradle project, you can add it as a file dependency (until published to a repository):

```gradle
dependencies {
    implementation files('libs/code-extractor-java-1.0-SNAPSHOT.jar')
}
```

Alternatively, if you are using it in a multi-project build:

```gradle
dependencies {
    implementation project(':code-extractor-java')
}
```

## Including in a Maven Project

Install the JAR to your local Maven repository:

```bash
mvn install:install-file \
   -Dfile=build/libs/code-extractor-java-1.0-SNAPSHOT.jar \
   -DgroupId=com.extractor \
   -DartifactId=code-extractor-java \
   -Dversion=1.0-SNAPSHOT \
   -Dpackaging=jar
```

Then add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.extractor</groupId>
    <artifactId>code-extractor-java</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
