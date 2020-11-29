# Autolog
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.maximevw/autolog)](https://search.maven.org/search?q=g:com.github.maximevw%20AND%20autolog)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/maximevw/autolog/Autolog%20CI%20Workflow)
![Code Coverage](autolog-coverage-reporting/resources/jacoco.svg)
[![Javadoc](https://javadoc.io/badge2/com.github.maximevw/autolog-core/javadoc.svg)](https://javadoc.io/doc/com.github.maximevw/autolog-core)

A library providing capabilities for automatic logging in Java applications.

## Modules
### autolog-core
This is the core module of the library: it provides the core logging features independently of the implementation chosen
for automation.

### autolog-aspectj
This module provides aspects for logging automation based on AspectJ weaving and using annotations defined in
`autolog-core` module.

### autolog-spring
This module is the implementation of the logging automation based on Spring AOP and using annotations defined in
`autolog-core` module. It acts as a Spring Boot starter by providing auto-configuration class for Autolog.

### autolog-coverage-reporting
This module is only used to generate a code coverage report for the entire project.

## Getting started
### Prerequisites

Using Autolog requires **JDK 11 or greater**.

### Installing

The latest release of Autolog is available on Maven Central. You can install Autolog in your application using one of
the following Maven dependencies:
* In a Spring application:
```xml
<dependency>
    <groupId>com.github.maximevw</groupId>
    <artifactId>autolog-spring</artifactId>
    <version>1.2.0</version>
</dependency>
```
* In an application using AspectJ weaving for logging automation by AOP:
```xml
<dependency>
    <groupId>com.github.maximevw</groupId>
    <artifactId>autolog-aspectj</artifactId>
    <version>1.2.0</version>
</dependency>
```
* In a classic Java application, you can use logging methods provided by Autolog without automation by AOP (not
recommended):
```xml
<dependency>
    <groupId>com.github.maximevw</groupId>
    <artifactId>autolog-core</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Usage
### Autolog annotations

Basically, Autolog provides two types of annotations used to automatically generates log thanks to [AOP](https://en.wikipedia.org/wiki/Aspect-oriented_programming)
(using [Spring AOP](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop) implementation
in the module `autolog-spring` or [AspectJ](https://www.eclipse.org/aspectj/) weaving in the module `autolog-aspectj`):

* **`@AutoLogMethodInOut`**, **`@AutoLogMethodInput`** and **`@AutoLogMethodOutput`**: it helps to log, respectively,
the input and output data of methods, the input data only and the output data only. It can also be used to automatically
log exceptions.
* **`@AutoLogPerformance`**: it helps to monitor and log the performance (execution time, failures, ...) of invoked
methods during the execution of an application. Please note that the usage of this annotation may impact the performance
of your application. If you need high performance during the execution of your code, don't use it (especially in
production).

These annotations can be located on classes or methods (or both, in this case the configuration of the annotations at
method level gets the priority). For further information about the configuration of each annotation, please consult the
Javadoc.

* **`@Mask`**: only located on method arguments, it allows masking (totally or partially) the values of arguments logged
thanks to the annotation `@AutoLogMethodInOut` or `@AutoLogMethodInput`.

### Loggers management

Each time Autolog will log something, it will use the loggers configured in a **`LoggerManager`** instance (which is a
Spring-managed bean in Spring applications). The `LoggerManager` maintains a list of all the loggers to use, so Autolog
is able to log the information at different places simultaneously. The real loggers instances managed by `LoggerManager`
are wrapped in singleton adapters implementing `LoggerInterface`. By default, Autolog provides the following adapters:
* `JavaLoggerAdapter`: wraps an instance of the classic `java.util.logging.Logger`
* `Log4j2Adapter`: wraps an instance of `org.apache.logging.log4j.Logger`
* `Slf4jAdapter`: wraps an instance of `org.slf4j.Logger`
* `LogbackWithLogstashAdapter`: wraps an instance of `org.slf4j.Logger` which should use Logback implementation with
Logstash encoder
* `SystemOutAdapter`: wraps the standard output (`System.out` and `System.err`)
* `XSlf4jAdapter`: wraps an instance of `org.slf4j.ext.XLogger`
* _(Experimental)_ `JdbcAdapter`: persists log events into a database using JDBC.
* _(Experimental)_ `FloggerAdapter`: wraps an instance of `com.google.common.flogger.AbstractLogger`.
* _(Experimental)_ `KafkaAdapter`: publishes log events into Kafka topics.

In Spring Boot applications, the `LoggerManager` can be configured in the application properties (by setting the list
of `LoggerInterface` implementations to register in the property `autolog.loggers`) thanks to the auto-configuration
class `AutologAutoConfiguration`.

### Usage with AspectJ weaving

In order to use AspectJ weaving for logging automation by AOP, in addition to the dependency to `autolog-aspectj`,
ensure to have `org.aspectj:aspectjrt` in your classpath and to compile your application using AspectJ weaving,
including the dependency to `autolog-aspectj` in the weaved dependencies.

At the starting of your application, insert the following code to instantiate the `LoggerManager` required by Autolog
for logging automation:
```java
public class HelloApplication {
    public static void main(final String[] args) {
        // Instantiate Autolog.
        final LoggerManager loggerManager = new LoggerManager();
        // Register any loggers you want to use. See LoggerManager documentation for further details.
        // loggerManager.register(...);
        AspectJLoggerManager.getInstance().init(loggerManager);

        // Put the code of your application here...
    }
}
```

Now, you can use Autolog annotations into your application.

### Basic example with a Spring application

Assuming your application is a REST API developed with Spring Web framework using an implementation of Slf4j for
logging.

Here is the main class of your application:
```java
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableAutoConfiguration
public class HelloApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }
}
```

The configuration file of your Spring application contains the following property:
```properties
autolog.loggers=Slf4jAdapter
```

Here is your web controller using Autolog annotations with default configuration:
```java
@RestController
@AutoLogMethodInOut
@AutoLogPerformance
public class HelloWebController {
    @Autowired
    private HelloService helloService;

    @GetMapping("/hello")
    public String sayHello(@RequestParam final String name) {
        return helloService.sayHello(name);
    }
}
```

Here is the service, called by the web controller, using Autolog annotations with default configuration:
```java
@Service
@AutoLogMethodInOut
@AutoLogPerformance
public class HelloService {
    public String sayHello(final String name) {
        return "Hello " + name + "!";
    }
}
```

When the API endpoint `GET /hello?name=John` is called, Autolog produces the following logs:
```
INFO  Entering HelloWebController.sayHello(name=John)
INFO  Entering HelloService.sayHello(name=John)
INFO  Exiting HelloService.sayHello() returning Hello John!
DEBUG Method HelloService.sayHello executed in 6 ms (started: 2019-12-30 14:30:25.602, ended: 2019-12-30 14:30:25.608).
INFO  Exiting HelloWebController.sayHello() returning Hello John!
DEBUG Method [GET] /hello executed in 10 ms (started: 2019-12-30 14:30:25.600, ended: 2019-12-30 14:30:25.610).
DEBUG Performance summary report for /hello:
DEBUG > /hello executed in 10 ms
DEBUG |_ > HelloService.sayHello executed in 6 ms
```

More examples are available in this [GitHub repository](https://github.com/maximevw/autolog-examples).

## Contributing

If you want to contribute to Autolog project, please read the content of CONTRIBUTING file.

## Versioning

Autolog uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## License

Autolog is distributed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

## Next features

Find below a non-exhaustive (and not sorted by priority) list of features we've planned to develop:
* **`@AutoLog` annotation and logging profiles**

  A new annotation `@AutoLog` grouping the behaviour of both annotations `@AutoLogMethodInOut` and `@AutoLogPerformance`
  and using a configuration file (`classpath:autolog.yaml` by default) to define the parameters used to generate logs.
  For example: using
  ```java
  @AutoLog(profile = "customProfile")
  public void exampleMethod() {
  }
  ```
  instead of
  ```java
  @AutoLogMethodInOut(/*custom parameters...*/)
  @AutoLogPerformance(/*custom parameters...*/)
  public void exampleMethod() {
  }
  ```

  In Spring Boot auto-configuration, the profiles could be directly defined in the application property
  `autolog.profiles`.

* **Log HTTP requests and responses**

  Automate the logging of HTTP requests and responses for applications providing a REST API (using JAX-RS or Spring
  Web Framework) by using servlet filters.

* **Loggers configuration by profile**

  Register different loggers in the different defined logging profiles. For example: a custom profile uses
  `SystemOutAdapter` and another one `Slf4jAdapter`.

  In a given profile, the used loggers could also be different: for example the performance information logging uses
  `SystemOutAdapter` and the logging of input/output data uses `Log4j2Adapter`.
