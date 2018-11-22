# Coverage Maven Plugin

Maven plugin for managing test coverage requirements.

## Usage

Add the following to the `<pluginManagement>` section of the project POM:

    <plugin>
        <groupId>com.cognitran.products</groupId>
        <artifactId>coverage-maven-plugin</artifactId>
        <version>${coverage-maven-plugin.version}</version>
    </plugin>

## Configuration

1. Add `<properties>` for coverage level (can be set to `0%` to be updated by the plugin later):

        <properties>
            ...
            <jacoco.coverage.line.minimum>0%</jacoco.coverage.line.minimum>
            <jacoco.coverage.branch.minimum>0%</jacoco.coverage.branch.minimum>
            ...
        </properties>
    
2. Configure the check execution for the `jacoco-maven-plugin`:

        <build>
            <plugins>
                ...
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>${jacoco-maven-plugin.version}</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>prepare-agent</goal>
                            </goals>
                        </execution>
                        <execution>
                            <id>jacoco-check</id>
                            <phase>verify</phase>
                            <goals>
                                <goal>check</goal>
                            </goals>
                            <configuration>
                                <rules>
                                    <rule>
                                        <limits>
                                            <limit>
                                                <counter>LINE</counter>
                                                <value>COVEREDRATIO</value>
                                                <minimum>${jacoco.coverage.line.minimum}</minimum>
                                            </limit>
                                            <limit>
                                                <counter>BRANCH</counter>
                                                <value>COVEREDRATIO</value>
                                                <minimum>${jacoco.coverage.branch.minimum}</minimum>
                                            </limit>
                                        </limits>
                                    </rule>
                                </rules>
                            </configuration>
                        </execution>
                        <execution>
                            <id>site</id>
                            <phase>site</phase>
                            <goals>
                                <goal>report-aggregate</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                ...
            </plugins>
        </build>

## Usage

1. `mvn clean install jacoco:report coverage:update-coverage-requirement` will update the properties.
2. Review & commit changes.

## Change Log

### 0.1.0 

- [[COVR-2](https://jira.cognitran.com/browse/COVR-2)] Initial implementation of `update-coverage-requirement` Mojo. 