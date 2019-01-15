/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("Changed branch code coverage: 25%")
assert buildLog.contains("[ERROR] 92.0% requirement for test coverage of changed branches not met.")