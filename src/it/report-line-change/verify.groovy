def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("Line 25: line not covered")