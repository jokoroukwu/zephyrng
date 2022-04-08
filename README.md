## ZephyrNG

A simple library for publishing TestNG test results using Zephyr for JIRA Server API, written in pure Kotlin and fully
interoperable with Java. The library uses the undocumented Zephyr for Jira server api which allows a lot more
flexibility and functionality, i.e. for now the following features are supported:

- Zephyr step mapping
- Publishing of data driven tests results

## Installation

For Gradle project you should add the dependency first:

```Groovy
dependencies {
    implementation "io.github.jokoroukwu:zephyrng:0.1.1"
}
```

Then register ZephyrNgListener to apply library functionality:

```Groovy
test {
    useTestNG() {
        listeners += ["io.github.jokoroukwu.zephyrng.TestNgZephyrAdapter"]
    }
}
```

Finally, you should add ```zephyrng-config.yml``` configuration file which should look like this:

```YAML
# The timezone used to display Zephyr test result start and end time
time-zone: GMT+3

# Your JIRA server project key
project-key: PROJKEY

# Your JIRA server URL
jira-url: https://your-jira-server:8089

# Your JIRA credentials.
username: ${username:?err}
password: ${password:?err}
```

Any placeholders like ```${password:?err}``` can be substituted with either environment variables or system properties
with environment variables taking precedence over system properties.<br><br>

The path to ```zephyr-config.yml``` file is resolved as follows (whichever succeeds first):

- If ```ZEPHYR_CONFIG``` environment variable is set, then its value is used as an absolute path
- If ```zephyr.config``` system property is set, then its value is used as an absolute path
- Finally, classpath resources are scanned for ```zephyr-config.yml``` file

## Licence

Copyright 2021 John Okoroukwu

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.