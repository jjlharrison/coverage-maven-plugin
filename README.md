# Coverage Maven Plugin

## Usage

    <!-- Must come after jacoco:report -->
    <plugin>
        <groupId>com.cognitran.products</groupId>
        <artifactId>coverage-maven-plugin</artifactId>
        <version>{{VERSION}}</version>
        <configuration>
            <compareBranch>develop</compareBranch> <!-- User property: coverage.change.branch -->
            <jacocoXmlReport>${project.reporting.outputDirectory}/jacoco/jacoco.xml</jacocoXmlReport>
            <skip>false</skip> <!-- User property: change-coverage.skip -->
            <xmlReportFile>${project.reporting.outputDirectory}/change-coverage/report.xml</xmlReportFile>
            <changedBranchCoverageRequirementPercentage>92</changedBranchCoverageRequirementPercentage> <!-- Used by check goal. User property: coverage.change.branch.requirement -->
            <changedLineCoverageRequirementPercentage>92</changedLineCoverageRequirementPercentage> <!-- Used by check goal. User property: coverage.change.line.requirement -->
        </configuration>
        <executions>
            <execution>
                <phase>post-site</phase> <!-- To run in an earlier or non-site phase, jacoco:report must also be moved to an earlier phase -->
                <goals>
                    <goal>report</goal>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

## Goals

### Report

The `report` goal will diff the current HEAD of the Git repository with a configured branch ("develop" by default) and check the JaCoCo report to determine what line and branch coverage is for changed and new lines.

The `jacoco:report` goal must have been executed before the `coverage:report` goal is executed. 

If the `compareBranch` is behind a remote tracking branch, the plugin will compare with that instead.

If the `compareBranch` is not found, but a remote branch with that name exists, the plugin will compare with that instead.

### Check

The `check` goal will read the change coverage report and will fail the build if the coverage levels fall below the requirements configured.

The `report` goal must have been executed before this goal is executed.

### Update Coverage Requirement

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

#### Usage

1. `mvn clean test jacoco:report coverage:update-coverage-requirement` will update the properties.
2. Review & commit changes.

## Change Log

### 0.2.0

- [[COVR-4](https://jira.cognitran.com/browse/COVR-4)] Add `report` and `check` goals to measure test coverage for changed code and optionally fail build if coverage isn't high enough. 

### 0.1.0 

- [[COVR-2](https://jira.cognitran.com/browse/COVR-2)] Initial implementation of `update-coverage-requirement` Mojo. 