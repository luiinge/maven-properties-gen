Maven Properties Generator
================================================================================

![GitHub](https://img.shields.io/github/license/luiinge/maven-properties-gen?style=plastic)
[![JaCoCo](https://img.shields.io/badge/dynamic/xml?style=plastic&label=coverage&query=floor%28%28%2Freport%2Fcounter%5B%40type%3D%27INSTRUCTION%27%5D%2F%40covered%29div%28%20%2Freport%2Fcounter%5B%40type%3D%27INSTRUCTION%27%5D%2F%40covered%20%2B%20%2Freport%2Fcounter%5B%40type%3D%27INSTRUCTION%27%5D%2F%40missed%20%29%2A100%29&suffix=%20%25&url=https%3A%2F%2Fraw.githubusercontent.com%2Fluiinge%2Fmaven-properties-gen%2Fmaster%2Fdocs%2Fjacoco%2Fjacoco.xml)](https://luiinge.github.io/maven-properties-gen/coverage)
![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/luiinge/maven-properties-gen/quality%20check/master?style=plastic)
![Maven Central](https://img.shields.io/maven-central/v/io.github.luiinge/maven-properties-gen?style=plastic)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=luiinge_maven-properties-gen&metric=alert_status)](https://sonarcloud.io/dashboard?id=luiinge_maven-properties-gen)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=luiinge_maven-properties-gen&metric=ncloc)](https://sonarcloud.io/dashboard?id=luiinge_maven-properties-gen)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=luiinge_maven-properties-gen&metric=bugs)](https://sonarcloud.io/dashboard?id=luiinge_maven-properties-gen)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=luiinge_maven-properties-gen&metric=code_smells)](https://sonarcloud.io/dashboard?id=luiinge_maven-properties-gen)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=luiinge_maven-properties-gen&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=luiinge_maven-properties-gen)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=luiinge_maven-properties-gen&metric=sqale_index)](https://sonarcloud.io/dashboard?id=luiinge_maven-properties-gen)

Sometimes it is desirable to access to properties defined in the Maven 
`pom.xml` file of your project from a Java process. The most straightforward solution is copy them 
as constants in some class, but then you will have the burden of maintaining those 
values up to date with every change in the `pom.xml`. 

There are workarounds that allow you to extract Maven properties to a
text file that can be read later from a Java process. However, such techniques are not suited for 
certain scenarios where static values are required (for instance, property values within 
annotations *must* be static). 

This annotation processor fills that gap, **creating a class with static constants and updating it
every time you compile your project**. 

Usage
--------------------------------------------------------------------------------

- Include the dependency in your `pom.xml` :
    ```xml
      <dependency>
          <groupId>io.github.luiinge</groupId>
          <artifactId>maven-properties-gen</artifactId>
          <version>1.0.0</version>
      </dependency>
    ```

- Create a new interface annotated with `@ProjectProperties`. Add as many methods as you 
want, annotated with `@ProjectProperty` and the Maven property identifier. For example:

    ```java
    package my.project;
  
    @ProjectProperties
    public interface ProjectInformation {
    
        @ProjectProperty("${project.version}")
        String staticVersion();
    
        @ProjectProperty("${project.description}")
        String description();
      
        @ProjectProperty("${project.inceptionYear}")
        int year();
  
    }
    ```
    

- According your Maven build configuration, you may need to add the processor path in your `pom
.xml`:
    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven.compiler.version}</version>
                <configuration>
                    <annotationProcessorPaths>
                        <annotationProcessorPath>
                            <groupId>io.github.luiinge</groupId>
                            <artifactId>maven-properties-gen</artifactId>
                            <version>1.0.0</version>
                        </annotationProcessorPath>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ```

And that is it. After compiling, a new class would have been generated in the folder 
`target/generated-sources/annotations`. Using the previous example, the output would be
something like the following:

```java
package my.project;
public final class StaticProjectInformation implements ProjectInformation {

    public static final String STATIC_VERSION = "1.0.0";
    public static final String DESCRIPTION = "This is the description of my project";
    public static final int YEAR = Integer.parseInt("2020");


    @Override
    public String staticVersion() {
        return STATIC_VERSION;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }
    
    @Override
    public int year() {
        return YEAR;    
    }   
}
```


Other considerations
----------------------------------------------------------------------------

### Property interpolation
You are not limited to reference properties explicitly declared in your `pom.xml`. Since this 
processor uses the Maven interpolation engine instead of plainly reading the file, you can 
reference implicit properties o even inherited properties from a parent project.

### Accepted types
Although there is no constraints related to the method names, they *must* return one of the
following types:
- `String`
- any of the primitive types (`byte`,`short`,`int`,`long`,`float`,`double`)
- any of the corresponding boxed types (`Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`)

The value parsing is performed using the proper existing methods of each type (for example, 
`Integer:parseInt` for `int`). Notice that the processor is not responsible for the correctness
of the values within the `pom.xml` file, so it may result in parsing errors at compilation time 
if they are not valid.

Also, attempting to use any other type will result in an compiler error as well.   

### Processor options
You can customize the package and name of the generated classes with the following options:

| option             | description                              | default value* 
| ------------------ | ---------------------------------------- | ------------- 
| `generatedClass`   | name of the generated class              | `Static%s`
| `generatedPackage` | enclosing package of the generated class | `%s`

*being `%s` a placeholder for the original name.

Authors
-----------------------------------------------------------------------------------------

- Luis Iñesta Gelabert  |  luiinge@gmail.com

Contributions
-----------------------------------------------------------------------------------------
If you want to contribute to this project, visit the
[Github project](https://github.com/luiinge/maven-properties-gen). You can open a new issue
 / feature
request, or make a pull request to consider. You will be added
as a contributor in this very page.

Issue reporting
-----------------------------------------------------------------------------------------
If you have found any defect in this software, please report it 
in [Github project Issues](https://github.com/luiinge/maven-properties-gen/issues). 
There is no guarantee that it would be fixed in the following version but it would 
be addressed as soon as possible.   
 

License
-----------------------------------------------------------------------------------------

```
    MIT License

    Copyright (c) 2020 Luis Iñesta Gelabert - luiinge@gmail.com

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
```
 

