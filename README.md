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
