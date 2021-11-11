def buildLog = new File((File) basedir, "build.log").getText()

assert buildLog.contains("Skipping.")
