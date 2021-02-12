<p align="center">
  <a href="https://www.scm-manager.org/">
    <img alt="SCM-Manager" src="https://download.scm-manager.org/images/logo/scm-manager_logo.png" width="500" />
  </a>
</p>
<h1 align="center">
  scm-ldap-plugin
</h1>

This plugin provides an Authentication for SCM manager using LDAP.

## Build and testing

The plugin can be compiled and packaged with the following tasks:

- build - `./gradlew build` - creates the final plugin bundle (smp package) in the build/lib folder
- check - `./gradlew check` - executes tests for Java and JavaScript
- clean - `./gradlew clean` - removes the build directory (should be used sparingly)

For the development and testing the `run` task of the plugin can be used:

- run - `./gradlew run` - starts scm-manager with the plugin pre installed.

If the plugin was started with `./gradlew run`, the default browser of the os should be automatically opened.
If the browser does not start automatically, start it manually and go to [http://localhost:8081/scm](http://localhost:8081/scm).

In this mode each change to web files (src/main/js or src/main/webapp), should trigger a reload of the browser with the made changes.

### Test-setup
To test this plugin against a ldap you may use a [preconfigured ldap inside a docker container](https://github.com/rroemhild/docker-test-openldap): 
```
docker pull rroemhild/test-openldap
docker run --privileged -d -p 389:389 rroemhild/test-openldap
```

Or simply use `docker-compose`:
```
docker-compose up
```

To connect against this ldap container you must set the following settings in your global ldap configuration:
* Select Profile => `Custom`
* Set Base DN to `dc=planetexpress,dc=com`
* Set Connection DN to `cn=admin,dc=planetexpress,dc=com`
* Set the Connection Password to `GoodNewsEveryone`
* Set search filter to `(&(objectClass=inetOrgPerson)(uid={0}))`
* Set group filter to `(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))`

Now you can test the connection with username `professor` and password `professor`.

## Directory & File structure

A quick look at the files and directories you'll see in a SCM-Manager project.

    .
    ├── build/
    ├── node_modules/
    ├── src/
    |   ├── main/
    |   |   ├── java/
    |   |   ├── js/
    |   |   └── resources/
    |   ├── test/
    |   |   ├── java/
    |   |   └── resources/
    ├── .editorconfig
    ├── .gitignore
    ├── CHANGELOG.md
    ├── LICENSE.txt
    ├── package.json
    ├── README.md
    ├── tsconfig.json
    └── yarn.lock

1.  **`build/`**: The build directory of this plugin into which Gradle generates all build artifacts

2.  **`node_modules/`**: This directory contains all of the modules of code that your project depends on (npm packages) are automatically installed.

3.  **`src/`**: This directory will contain all of the code related to what you see or not. `src` is a convention for “source code”.

    1. **`main/`**
       1. **`java/`**: This directory contain the Java code.
       2. **`js/`**: This directory contains the JavaScript code for the web ui, inclusive unit tests: suffixed with `.test.ts`
       3. **`resources/`**: This directory contains the the classpath resources.
    2. **`test/`**
       1. **`java/`**: This directory contains the Java unit tests.
       2. **`resources/`**: This directory contains classpath resources for unit tests.

4.  **`.editorconfig`**: This is a configuration file for your editor using [EditorConfig](https://editorconfig.org/). The file specifies a style that IDEs use for code.

5.  **`.gitignore`**: This file tells git which files it should not track / not maintain a version history for.

6.  **`build.gradle`**: Gradle build script

7.  **`CHANGELOG.md`**: All notable changes to this project will be documented in this file.

8.  **`gradle.properties`**: Plugin-specific Gradle configuration properties, such as version

9.  **`LICENSE.txt`**: This project is licensed under the MIT license.

10.  **`package.json`**: Here you can find the dependency/build configuration and dependencies for the frontend.

11.  **`README.md`**: This file, containing useful reference information about the project.

12. **`settings.gradle`** The plugin’s settings file 

13. **`tsconfig.json`** This is the typescript configuration file.

14. **`yarn.lock`**: This is the ui dependency configuration.

## Need help?

Looking for more guidance? Full documentation lives on our [homepage](https://www.scm-manager.org/docs/) or the dedicated pages for our [plugins](https://www.scm-manager.org/plugins/). Do you have further ideas or need support?

- **Community Support** - Contact the SCM-Manager support team for questions about SCM-Manager, to report bugs or to request features through the official channels. [Find more about this here](https://www.scm-manager.org/support/).

- **Enterprise Support** - Do you require support with the integration of SCM-Manager into your processes, with the customization of the tool or simply a service level agreement (SLA)? **Contact our development partner Cloudogu! Their team is looking forward to discussing your individual requirements with you and will be more than happy to give you a quote.** [Request Enterprise Support](https://cloudogu.com/en/scm-manager-enterprise/).
