/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
def pom = new File((File) basedir, "pom.xml").getText()
assert pom.contains("<jacoco.coverage.line.minimum>10%</jacoco.coverage.line.minimum>")
assert pom.contains("<jacoco.coverage.branch.minimum>8%</jacoco.coverage.branch.minimum>")