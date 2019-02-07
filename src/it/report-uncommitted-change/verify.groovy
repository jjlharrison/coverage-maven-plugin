/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("Sample.java")
assert buildLog.contains("Line 28: line not covered")