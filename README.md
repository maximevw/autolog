# Autolog
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
![Code Coverage](autolog-coverage-reporting/resources/jacoco.svg)

A library providing capabilities for automatic logging in Java applications.

## Modules
### autolog-core
This is the core module of the library: it provides the core logging features independently of the implementation chosen
for automation.

### autolog-spring
This module is the implementation of the logging automation based on Spring AOP and using annotations defined in
`autolog-core` module. It acts as a Spring Boot starter by providing auto-configuration class for Autolog.

### autolog-coverage-reporting
This module is only used to generate a code coverage report for the entire project.

## Getting started
### Prerequisites

Using Autolog requires **JDK 11 or greater**.

### Installing

You can install Autolog in your application using one of the following Maven dependencies:
* In a Spring application:
```xml
<dependency>
    <groupId>com.github.maximevw</groupId>
    <artifactId>autolog-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```
* In a classic Java application, you can use logging methods provided by Autolog without automation by AOP (not
recommended):
```xml
<dependency>
    <groupId>com.github.maximevw</groupId>
    <artifactId>autolog-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
### Autolog annotations

Basically, Autolog provides two type of annotations used to automatically generates log thanks to [AOP](https://en.wikipedia.org/wiki/Aspect-oriented_programming)
(using [Spring AOP](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop) implementation
in the module `autolog-spring`):

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

### Loggers management

Each time Autolog will log something, it will use the loggers configured in a **`LoggerManager`** instance (which is a
Spring-managed bean in Spring applications). The `LoggerManager` maintains a list of all the loggers to use, so Autolog
is able to log the information at different places simultaneously. The real loggers instances managed by `LoggerManager`
are wrapped in singleton adapters implementing `LoggerInterface`. By default, Autolog provides the following adapters:
* `JavaLoggerAdapter`: wraps an instance of the classic `java.util.logging.Logger`
* `Log4j2Adapter`: wraps an instance of `org.apache.logging.log4j.Logger`
* `Log4jAdapter`: wraps an instance of `org.apache.log4j.Logger`
* `Slf4jAdapter`: wraps an instance of `org.slf4j.Logger`
* `SystemOutAdapter`: wraps the standard output (`System.out` and `System.err`)
* `XSlf4jAdapter`: wraps an instance of `org.slf4j.ext.XLogger`

In Spring Boot applications, the `LoggerManager` can be configured in the application properties (by setting the list
of `LoggerInterface` implementations to register in the property `autolog.loggers`) thanks to the auto-configuration
class `AutologAutoConfiguration`.

### Basic example

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

* **Implementation using AspectJ weaving**

  In addition to the Spring AOP implementation, a new module `autolog-aspectj` provides aspects to handle Autolog
  annotations in applications using AspectJ without Spring Framework.

* **Arguments masking**

  When the input data of a method call are logged by Autolog, some method arguments (e.g. sensitive data like passwords)
  can be partially or totally masked. For example, the result would be:
  ```
  Entering exampleMethod(username=john, password=***, cardNumber=416********5)
  ```

* **Logstash Logback encoder**

  For applications using Logback implementation of Slf4j and feeding Logstash with these logs, add the possibility to
  format the logs generated by Autolog in a structured way (using `StructuredArguments`) to make them easily searchable
  in Kibana.

* **Custom message templates for `@LogPerformance` annotation**

  Make the messages relative to the performance information of methods invocations customizable (as for the input/output
  data annotations).

* **Additional loggers**

  Provide new implementations of `LoggerInterface`:
  * `JdbcLogAdapter`: to directly store the generated logs in a database using JDBC.
  * `KafkaAdapter`: to directly publish the generated logs in Kafka topics.

* **Log HTTP requests and responses**

  Automate the logging of HTTP requests and responses for applications providing a REST API (using JAX-RS or Spring
  Web Framework) by using servlet filters.

* **Loggers configuration by profile**

  Register different loggers in the different defined logging profiles. For example: a custom profile uses
  `SystemOutAdapter` and another one `Slf4jAdapter`.

  In a given profile, the used loggers could also be different: for example the performance information logging uses
  `SystemOutAdapter` and the logging of input/output data uses `Log4jAdapter`.
