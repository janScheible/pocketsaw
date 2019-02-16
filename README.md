[adding-uses-workflow]: adding-uses-workflow.gif "Adding of uses relations"
[pocketsaw-package-group-structure]: pocketsaw-package-group-structure.png "Pocketsaw package group structure"
[pocketsaw-layered-sub-modules]: pocketsaw-layered-sub-modules.png "Pocketsaw layered sub-modules"
[angular-tour-of-heroes-dependencies]: angular-tour-of-heroes-dependencies.png "Angular Tour of Heros Dependencies"
[disid-multimodule-spring-boot]: disid-multimodule-spring-boot.png "DISID Spring Boot Multimodule"

# Pocketsaw

Compile time sub-module system, aimed at package group dependency organization within a Maven project resp. Java 9 module (and even more, see for example [Using Pocketsaw in an Angular project](#using-pocketsaw-in-an-angular-project)).

## Motivation

Package cycles are bad.
Especially between packages on the same level.
They make the codebase harder to change (if A depends on B and vice versa, then changing A would most likely require to also change B and both will be effectively one thing) and understand.

That package cycles should be avoided is an open secret (see [SEI CERT Oracle Coding Standard for Java
SEI CERT Oracle Coding Standard for Java at Carnigen Mellon Software Engineering](https://wiki.sei.cmu.edu/confluence/display/java/DCL60-J.+Avoid+cyclic+dependencies+between+packages)).
But as for example Jens Schauder mentions in his [blog entry](http://blog.schauderhaft.de/2011/07/17/breaking-dependency-cylces/) the awareness among developers seems not to be that high.
[This awesome article](https://dzone.com/articles/structure-spring-core) sheds some light on the internal package organization of the Spring Framework.
It shows that the Spring guys really take care of their dependencies (in contrast to other as well shortly mentioned Open Source projects).
And this was the main motivation for creating Pocketsaw: Having an easy and lightweight tool to model and check the internal package structure.

The following image shows the Pocketsaw package group structure.
Yellow boxes are sub-modules while the blue ones are external functionalities.
The gray ones are a special case, they represent the shaded libraries which are modeled as sub-modules because they are part of the codebase (see [Shaded dependencies](#shaded-dependencies)).
In case of a not allowed code dependency there would be a red arrow whilst in case of a defined but not used in the code dependency a gray arrow would be displayed.

![pocketsaw-package-group-structure]

Since version 1.2.0 there is an additional visualization available.
In the layered sub modules view all allowed dependencies go from top to bottom.
Horizontal or bottom to top dependencies are not allowed and marked red.

![pocketsaw-layered-sub-modules]

## Background

Highly inspired by the awesome [Jabsaw](https://github.com/ruediste/jabsaw) project.
And yes, pocketsaw instead of jabsaw (which is already a smaller jigsaw) is an intended pun. ;-)

The main differences are:
- visualization with a HTML template using vis.js (instead of graphviz/dot)
- default source of code/package dependency information is the completely underrated [jdeps](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html) command-line tool of the JDK (instead of the ASM library)
- package groups within the Maven project (resp. Java module) are called sub-module (instead of module to avoid name collision [Java Platform Module System](http://openjdk.java.net/projects/jigsaw/spec/))
- package groups outside the Maven project are called external functionality

## Installation

The Maven artifacts can't be found in an official repository yet ([JitPack](https://jitpack.io) usage is pending until [this issue](https://github.com/jitpack/jitpack.io/issues/2872) is resolved).

For a local installation the following is enough:

```
git clone git@github.com:janScheible/pocketsaw.git
cd pocketsaw
mvn install
```

## Workflow for using Pocketsaw in a Java project

### Adding of Maven dependency

Add
```xml
<dependency>
    <groupId>com.scheible.pocketsaw.api</groupId>
    <artifactId>pocketsaw-api</artifactId>
    <version>1.0.2</version>
</dependency>
```
and
```xml
<dependency>
    <groupId>com.scheible.pocketsaw.impl</groupId>
    <artifactId>pocketsaw-impl</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```
to project.

If not a Spring based project add
```xml
<dependency>
    <groupId>io.github.lukehutch</groupId>
    <artifactId>fast-classpath-scanner</artifactId>
    <version>2.21</version>
    <scope>test</scope>
</dependency>
```
as well.
Currently [FastClasspathScanner](https://github.com/lukehutch/fast-classpath-scanner) is the only other supported classpath scanner.
But a custom one can simply be used by extending the class `com.scheible.pocketsaw.impl.descriptor.ClasspathScanner` and using any already available classpath scanning facilities (for example [Reflections](https://github.com/ronmamo/reflections)).

### Execution of Pocketsaw in the build

Create a unit test like:
```java
public class PocketsawSubModulesTest {

    private static Pocketsaw.AnalysisResult result;

    @BeforeClass
    public static void beforeClass() {
        result = Pocketsaw.analizeCurrentProject(SpringClasspathScanner.create(PocketsawSubModulesTest.class));
    }
	
    @Test
        public void todo() {
    }
}
```

It is assumed that the test class is created in the root package of the project because classpath scanning will start with the package of the class used for classpath scanner creation.
For non Spring projects use `FastClasspathScanner.create(...)` and for Spring based ones `SpringClasspathScanner.create(...)`.

### Matching of all packages with sub-modules and external functionalities

Add `@SubModule` and `@ExternalFunctionality` annotated classed until every package is matched and the unit test passes.
In case of not yet matched package a error message like `UnmatchedPackageException: The package 'com.scheible.javasubmodules.impl.visualization' was not matched at all!` is displayed and either a sub-module or an external functionality has to be added. 
`@SubModule` annotated classes have to be placed in the root package of the sub-module. 
The default is that all sub-packages are include as well but this behavor can be override by `includeSubPackages = false`.
For external functionalities a package match pattern has to specified.
The syntax supports Ant style pattern (e.g. `com.test.*` matches all classes in the `com.test` package and `com.test.**` matches the classes in the sub-packages too).

The following conventions might be used:
* `@SubModule` annotated classes have the suffix `SubModule`.
  ```java
  package com.scheible.pocketsaw.impl.visualization;
  
  /**
   * Sub-module for visualizing the dependency graph.
   */
  @SubModule
  public class VisualizationSubModule { 
  }
  ```
* `@ExternalFunctionality` are collected as inner static classes in a class called `ExternalFunctionalities` in the root package of the project.
  ```java
  package com.scheible.pocketsaw.impl;
  
  public class ExternalFunctionalities {
  
      @ExternalFunctionality(packageMatchPattern = "org.springframework.beans.**")
      public static class SpringBeans {
      }
  }
  ```
  
As soon as all packages are matched the dependency graph HTML is generated.
It can be found in `./target/pocketsaw-dependency-graph.html`.
The full path is also printed on standard out while analyzing the project.
  
### Definition of the allowed sub-module dependencies

After every package is matched, uses relations have to be added to the sub-modules until all arrows are green.
Uses relations are defined in the `@SubModule` annotation like `@SubModule({SpringBeans})` or `@SubModule(includeSubPackages = false, uses = {{SpringBeans})` in case of multiple values.

The following sequence illustrates that process:
![adding-uses-workflow]

### Automatic enforcement of allowed dependencies

To make sure that the sub-modules and their dependencies are verify automatically replace the `todo()` test with:
```java
@Test
public void testNoDescriptorCycle() {
    assertThat(result.getAnyDescriptorCycle()).isEmpty();
}

@Test
public void testNoCodeCycle() {
    assertThat(result.getAnyCodeCycle()).isEmpty();
}

@Test
public void testNoIllegalCodeDependencies() {
    assertThat(result.getIllegalCodeDependencies()).isEmpty();
}
```

In this example [AssertJ](http://joel-costigliola.github.io/assertj/) is used and displays nice error messages in case of one of the asserts is violated.

In the future the unit test might fail when new packages or additional libraries are added.
The approach described in [Matching of all packages with sub-modules and external functionalities](#matching-of-all-packages-with-sub-modules-and-external-functionalities) is used than.

It might also fail if one of the asserts are vialoted.
In this case either the code has to be fixed to remove the not allowed code dependency or an additional usage relation has to be added like described in [Definition of the allowed sub-module dependencies](#definition-of-the-allowed-sub-module-dependencies).

## Using Pocketsaw in an Angular project

Since version 1.1.0 of Pocketsaw in addition to Java-only projects it can be used for asserting the sub-module structure of projects containing an Angular frontend as well.

The first step is to install Dependency Cruiser with `npm install --save-dev dependency-cruiser`.
Next it is easiest to add the following to the `scripts` section of the `package.json`:
```json
"dependencies": "dependency-cruise --ts-pre-compilation-deps -T json --exclude \"^node_modules\" src > dependencies.json"
```

Pocketsaw can then be run via the  CLI:
```
java -jar pocketsaw-1.3.0.jar sub-module.json dependencies.json dependency-cruiser pocketsaw-dependency-graph.html --ignore-illegal-code-dependencies
```

### Angular Tour Of Heros Example

In the following the structure of an [Angular Tour Of Heroes](https://github.com/rpoitras/angular-tour-of-heroes) example is visualized:

![angular-tour-of-heroes-dependencies]

The good news is that the "children" of `App` have no dependencies with their siblings at all.
Also, the two-way relation between them and `App` could perhaps easily be resolved by moving `HeroService` and `MessageService` to dedicated sub-directories and therefore sub-modules.

## Using Pocketsaw with a Spring Boot JAR

Since version 1.3.0 there is also support for analyzing Spring BOOT JARs "from the outside".
That means instead of using annotations in the code an external `sub-module.json` is used.
The use case is to analyze an unmodified code base that does (not yet) use Pocketsaw.

```
java -jar pocketsaw-1.3.0.jar sub-module.json target/spring-boot-app.jar spring-boot-jar:root-packages=sample.multimodule target/pocketsaw-dependency-graph.html --ignore-illegal-code-dependencies
```

### Spring Boot Multimodule Example

In the following the structure of a [Spring Boot Multimodule](https://github.com/DISID/disid-proofs/tree/master/spring-boot-multimodule) project is visualized:

![disid-multimodule-spring-boot]

## CLI 

Since version 1.1.0 there is CLI support available via the `com.scheible.pocketsaw.impl.cli.Main` class.

```
usage: pocketsaw <sub-module.json> <dependencies.file> {dependency-cruiser|spring-boot-jar} <pocketsaw-dependency-graph.html> 
           [--ignore-illegal-code-dependencies] [--verbose]
```

Dependency information sources might require specific parameters to be passed.
The format for that is `dependency-source:foo=bar:value=42`.

### Sub-modules descriptors

For CLI usage the sub-modules descriptors are read from a JSON file.
The file format looks like this:

```json
{
    "subModules": [
        {
            "name": "First",
            "packageName": "project.first",
            "includeSubPackages": false,
            "color": "red"
        }, {
            "name": "FirstChild",
            "packageName": "project.first.child",
            "uses": ["First"]
        }
    ],
    "externalFunctionalities": [
        {
            "name": "Spring",
            "packageMatchPattern": "org.springframework.*"
        }
    ]
}
```
- sub-modules
  - `uses` with `[]` as default
  - `includeSubPackages` with default same as `@SubModule#includeSubPackages` (`true`)
  - `color` with default same as `@SubModule#color` (`orange`)
- external functionalities (optional, depends on specific dependency source if supported)

### Third-party dependencies information source

To add an third-party dependency information source the interface [`PackageDependencySource`](pocketsaw-impl/src/main/java/com/scheible/pocketsaw/impl/code/PackageDependencySource.java) has to be implemented in an separated Maven project.
Pocketsaw then uses JDK's `ServiceLoader` to find and load the dependency source.
Therefore a no-args constructor is mandatory.
For an example of such an implementation see the one of [Dependency Cruiser](pocketsaw-dependecy-cruiser).

#### Dependency Cruiser dependency information source

No parameters supported.

**NOTE**: For now the reported dependencies are limited to TypeScript files excluding all `*.spec.ts`.

**NOTE**: Currently no external functionalities are supported.

#### Spring Boot JAR dependency information source

Required parameters:
- `root-packages`: Comma-separated list of root packages

Optional parameters:
- `keep-temp-dir-contents`: Skips deletion of used temp directory
- `temp-dir-name`: Custom temp directory name instead of random UUID

### Maven usage

To automated Pocketsaw execution in a Maven project with an Angular frontend the `exec-maven-plugin` can be use like this:
```xml
<plugin>
	<groupId>org.codehaus.mojo</groupId>
	<artifactId>exec-maven-plugin</artifactId>
	<version>1.6.0</version>
	<executions>
		<execution>
			<id>pocketsaw</id>
			<goals>
				<goal>exec</goal>
			</goals>
			<phase>verify</phase>
			<configuration>
				<executable>java</executable>
				<classpathScope>test</classpathScope>
				<arguments>
					<argument>-classpath</argument>
					<classpath/>
					<argument>com.scheible.pocketsaw.impl.cli.Main</argument>
					<argument>${project.basedir}/pocketsaw-sub-modules.json</argument>
					<argument>${project.basedir}/target/dependency-cruiser-dependencies.json</argument>
					<argument>dependency-cruiser</argument>
					<argument>${project.basedir}/target/pocketsaw-dependency-graph.html</argument>
					<argument>--verbose</argument>
				</arguments>
			</configuration>
		</execution>
	</executions>				
</plugin>
```

### CLI exit codes

The CLI uses the following exit codes to allow easy scripting:

| exit code | description                                         |
|-----------|-----------------------------------------------------|
| -1        | unexpected fatal error                              |
| 1         | no package dependency source was found on classpath |
| 2         | not enough arguments                                |
| 3         | something wrong with an argument                    |
| 4         | found a descriptor cycle                            |
| 5         | found a code cycle                                  |
| 6         | found illegal code dependencies                     |

## Shaded dependencies

To avoid unnecessary Maven dependency conflicts the following libraries are included shaded:

* `AntPathMatcher` and dependencies of [org.springframework:spring-core:5.0.6](https://github.com/spring-projects/spring-framework/blob/v5.0.6.RELEASE/spring-core/src/main/java/org/springframework/util/AntPathMatcher.java) ([license](spring-framework-LICENSE)) in the package `com.scheible.pocketsaw.impl.shaded.org.springframework`
* [com.eclipsesource.minimal-json:minimal-json:0.9.5](https://github.com/ralfstx/minimal-json/tree/0.9.5) ([license](minimal-json-LICENSE)) in the package `com.scheible.pocketsaw.impl.shaded.com.eclipsesource`

## Licencse

[MIT License](LICENSE)
