/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("[WARNING] New file com/cognitran/products/Sample.java: 6/7 changed lines covered, 3/4 changed branches covered")
assert buildLog.contains("[INFO] Changed branch code coverage: 75%")
assert buildLog.contains("[INFO] Changed line code coverage: 85.71%")
