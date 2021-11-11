def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("No new code found.")
def changeCoverage = new XmlSlurper().parse(new File((File) basedir, "target/site/change-coverage/report.xml"))
assert changeCoverage.summary.branch == "100.0"
assert changeCoverage.summary.line == "100.0"