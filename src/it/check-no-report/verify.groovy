def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("Change coverage report not found")
