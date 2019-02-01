/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
def changeCoverageLog = new File((File) basedir, "target/change-coverage.log").getText()

assert changeCoverageLog.contains("Project: coverage-maven-plugin-change-coverage-test-module-1")
assert changeCoverageLog.contains("Project: coverage-maven-plugin-change-coverage-test-module-2")