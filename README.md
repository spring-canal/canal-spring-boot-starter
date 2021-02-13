# Canal Spring Boot Starter

A Spring Boot Canal support

- Gradle
    ```groovy
    repositories {
        maven {
            url 'https://dl.bintray.com/spring-canal/maven'
        }
    }

    dependencies {
        implementation 'com.alibaba:canal-spring-boot-starter:1.1.4'
    }
    ```
- Maven
  ```xml
  <project>
      <repositories>
          <repository>
              <id>bintray-spring-canal-maven</id>
              <name>bintray</name>
              <url>https://dl.bintray.com/spring-canal/maven</url>
          </repository>
      </repositories>

      <dependencies>
          <dependency>
              <groupId>com.alibaba</groupId>
              <artifactId>canal-spring-boot-starter</artifactId>
              <version>1.1.4</version>
          </dependency>
      </dependencies>
  </project>
  ```

# License

Canal Spring Boot Starter is released under the terms of the Apache Software License Version 2.0 (see LICENSE).

# Reference

[Spring Canal](https://github.com/spring-canal/spring-canal)
