def pom = new File((File) basedir, "pom.xml").getText()
assert !pom.contains("<jacoco.coverage.line.minimum>")
assert !pom.contains("<jacoco.coverage.branch.minimum>")