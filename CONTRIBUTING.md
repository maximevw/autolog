# Contributing to Autolog

Autolog is an open source project and since all the constructive ideas and remarks are appreciated, you're welcome if
you want to contribute to this project!

When contributing to Autolog project, please first discuss the change you wish to make via an issue or any other method
with the owners of this repository before making a change.

## Report a bug

If you think you have found a bug in Autolog, first make sure that you are testing against the latest version of the
project. If not, maybe your issue has already been fixed in a more recent version.
Otherwise, search [our issues list on GitHub](https://github.com/maximevw/autolog/issues) if a similar issue has not
already been opened. If you didn't find an existing similar issue, feel free to
[create a new one](https://help.github.com/en/github/managing-your-work-on-github/creating-an-issue) respecting the
following recommendations:

* Provide a quick summary of the problem.
* Prepare a reproduction of the bug by preparing a simple test case we can run to reproduce your bug. It will be helpful
to find and fix the problem.
* Describe the result you expected.
* Don't hesitate to provide as much information as you can. Often, the devil is in the detail!

## Contributing code

Autolog requires JDK 11 and Maven to be built. This project is originally developed with IntelliJ Community Edition and
several run configurations are available in the directory `.idea/runConfigurations`.

### Fork and clone the repository

To contribute to this project, you will need to fork its repository and clone it to your local machine.
See [GitHub help page](https://help.github.com/articles/fork-a-repo) for help.

The command to type in order to clone the forked repository to your local machine should be:
```
git clone https://github.com/{your username}/autolog.git
```

### Configure the project and run tests

Once you cloned the repository to you local machine, open the project in your favorite IDE.

To build the project, execute the following command:
```
mvn clean install
```

To run the tests, execute the following command:
```
mvn test
```

#### Maven profiles

Autolog project provides some useful profiles to help developers to guarantee the quality of the code, of the tests and
reduce potential vulnerabilities:
* **Dependency check**: to detect vulnerabilities in third party dependencies, you can run the build with the profile
`dependency-check` executing the plugin `dependency-check-maven` from OWASP and generating a report in `target/owasp`
directory.
```
mvn clean install -Pdependency-check
```
* **Code coverage**: by default, the module `autolog-coverage-reporting` generates a global coverage report for the
whole project. However, the coverage reports can be generated separately for each by activating the profile
`with-coverage-by-module`. It is useful to check if the coverage requirements are met for each module.
```
mvn clean install -Pwith-coverage-by-module
```

*Note*: a profile `release` also exists and is designed to prepare and build release versions of Autolog (compilation,
signing artifacts, ...). It is reserved for the project owners.

### Submit a pull request

Once your changes and tests are ready for review, submit them:

1. Be sure that your changes are tested, the tests run with success and reach the minimal coverage rules:
    * 70% of instructions covered by tests
    * 80% of branches covered by tests

2. Check your code is documented (especially if it modifies the public API) and respect the coding style of the project.

3. Rebase your changes: update your local repository with the most recent code from the original repository, and rebase
your branch on top of the latest master branch. It is better that your initial changes are squashed into a single
commit. If more changes are required to validate the pull request, we invite you to add them as separate commits.

4. Finally, push your local changes to your forked repository and submit a pull request with a title which sums up the
changes that you have made (try to not exceed 50 characters), and provide more details in the body. If necessary, also
mention the number of the issue solved by your changes, e.g. "Closes #123".

### Coding conventions and documentation

Autolog comes with a set of Checkstyle rules and an `.editorconfig` file to ease the respect of some basic formatting
rules. We invite you to follow them when you contribute to this project.

Please find here some of the main coding guidelines:
* Java indent is a tabulation equivalent to 4 spaces.
* Maximal line length is 120 characters.
* The trailing whitespaces must be trimmed.
* Maximal file length is 1500 lines.
* Respect the standard Java naming conventions.
* Wildcards imports (e.g. `import com.foo.bar.*`) and redundant ones are forbidden.
* Empty `catch` blocks aren't allowed.
* Left braces are located at the end of the line.
* Braces are mandatory around conditional and loops blocks.
* Don't write more than one statement by line.
* Ensure you don't call `equals()` on nullable values.

You can configure Checkstyle plugin in IntelliJ or Eclipse to use the rules defined in the file `checkstyle.xml` to
verify that your changes respect our code conventions.

#### Javadoc

Good Javadoc can help with navigating and understanding code. That's why we provide here some guidelines to write
Javadoc for Autolog. Please note that the Javadoc is also checked by Checkstyle.

1. Always add Javadoc to new code.
2. Don't document anything trivial or obvious (e.g. getters and setters). The documentation should add value to the
code.
3. Javadoc must explain the purpose of a feature/function/class (the "why"), not the "how" (i.e. the implementation
itself), except if it is helpful for the understanding of the "why".
4. All the public classes and methods must be documented. But do not hesitate to also document private ones: it will be
helpful for other developers to understand the code and how to use it.
5. If missing and if you can do it, add Javadoc to existing code.
6. Use `@link` and `@see` to add references, either to related resources in the codebase or to relevant external
resources. Be careful with usage of links to codebase resources in annotations processors (see specific points of
attention below).
7. Do not hesitate to document test methods: succinctly describe preconditions, actions and expectations of the tests.

#### Unit tests

Each class of unit tests must be suffixed by `Test` and located in the same package as the tested class. For example,
the tests relative to the class `com.github.maximevw.autolog.core.logger.LoggerManager` will be implemented in the
class `com.github.maximevw.autolog.core.logger.LoggerManagerTest`. The tested instances are generally declared with the
name `sut` (i.e. "subject under test").

The name of the test methods should respect the
["given-when-then" convention](https://martinfowler.com/bliki/GivenWhenThen.html) inspired by
Behavior-Driven-Development:
* **given** part: briefly describes the specific pre-conditions of the test
* **when** part: indicates the tested method
* **then** part: briefly describes the expected result (e.g. `returnsValue`, `throwsException`, ...)

A typical unit test should be written like that:
```java
void givenPreConditions_whenDoSomething_getsExpectedResult() {
    // Implementation of the unit test here.
    final Object actualResult = sut.doSomething(preconditions);
    assertEquals(expectedResult, actualResult);
}
```

#### Specific points of attention

* **NEVER** import references to other classes of Autolog project in the classes of package
`com.github.maximevw.autolog.core.annotations.processors` since these classes are compiled separately: it will avoid
`MethodNotFoundException`, `NoClassDefFoundError` and other compilation errors.

### License headers

Autolog is distributed under Apache License 2.0. The license headers are required on all Java files. They will be
automatically generated by Maven plugin `license-maven-plugin` at compilation time if they are missing. So, all
contributed code should have the following license header:
```
/*-
 * #%L
 * {Autolog module name defined in POM of each module}
 * %%
 * Copyright (C) 2019 Maxime WIEWIORA
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
```