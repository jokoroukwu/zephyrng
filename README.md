# ZephyrNG

A simple library for publishing TestNG test results using Zephyr for JIRA Server API, written in pure Kotlin and fully
interoperable with Java. The library uses the undocumented Zephyr for Jira server api which allows a lot more
flexibility and functionality, i.e. for now the following features are supported:

- Zephyr step mapping
- Publishing of data driven tests results

The following section describes how to configure the library to use it as a standalone.<br>
If your project uses Gradle you may consider
using [zephyr-gradle-plugin](https://github.com/jokoroukwu/zephyr-gradle-plugin) instead.

## Setup

Simply add the dependency and register the listener:

- ### Gradle

```Groovy
dependencies {
    implementation "io.github.jokoroukwu:zephyrng:0.1.1"
}

test {
    useTestNG() {
        listeners += ["io.github.jokoroukwu.zephyrng.TestNgZephyrAdapter"]
    }
}
```

- ### Maven

```XML

<dependency>
    <groupId>io.github.jokoroukwu</groupId>
    <artifactId>zephyrng</artifactId>
    <version>0.1.1</version>
</dependency>

<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-surefire-plugin</artifactId>
<version>${plugin.version}</version>
<configuration>
    <properties>
        <property>
            <name>listener</name>
            <value>io.github.jokoroukwu.zephyrng.TestNgZephyrAdapter</value>
        </property>
    </properties>
</configuration>
</plugin>
```

### YAML configuration

Some additional configuration is also required and can be specified in ```zephyr-config.yml``` file.<br>
The file should have the following format:

```YAML
# The timezone used to display Zephyr test run start and end time
time-zone: GMT+3

# Your JIRA server project key
project-key: PROJKEY

# Your JIRA server URL
jira-url: https://${your.jiraserver.address}

# Your JIRA credentials.
username: ${username}
password: ${password}
```

Any placeholders such as ```${password:?err}``` will be substituted with either environment variables or system
properties of the respective name. Environment variables take precedence over system properties.<br><br>

The path to ```zephyr-config.yml``` file is resolved as follows (whichever succeeds first):

- If ```ZEPHYR_CONFIG``` environment variable is set, then its value is used as an absolute path.
- If ```zephyr.config``` system property is set, then its value is used as an absolute path.
- Finally, classpath resources are scanned for ```zephyr-config.yml``` file.

## Usage

A test method needs to be annotated with ```@TestCaseKey```
to have its result collected, properly mapped to the existing test case and published to Zephyr. Example:

```java
public class MyTestClass {

    @Test
    @TestCaseKey("MYPROJ-123")
    public void should_test_something() {

    }
}
```

You may optionally annotate any method with ```@Step```. This will map the method's result to the corresponding Zephyr
step. Example:

```Java
public class MyTestClass {

    @Step(value = 0, description = "create user")
    public void createUser(User user) {

    }
}
```

## Licence

Copyright 2021 John Okoroukwu

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.