/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("[WARNING] New file com/cognitran/products/Sample.java: 6/7 changed lines covered, 3/4 changed branches covered")
assert buildLog.contains("[INFO] Changed branch code coverage: 75%")
assert buildLog.contains("[INFO] Changed line code coverage: 85.71%")

def changeCoverage = new XmlSlurper().parse(new File((File) basedir, "target/site/change-coverage/report.xml"))
assert changeCoverage.summary.branch == "75.0"
assert changeCoverage.summary.line.toString().startsWith("85")