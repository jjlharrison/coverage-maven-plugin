

import org.eclipse.jgit.api.Git

import java.nio.file.Files
import java.nio.file.StandardCopyOption

def git = Git.init().setDirectory((File) basedir).call()

def sampleFilePath = "src/main/java/com/jjlharrison/Sample.java"
def file = new File((File) basedir, sampleFilePath)
assert file.exists()

new File((File) basedir, ".gitignore") << '''
target
Sample.new.java
build.log
invoker.properties
prebuild.groovy
'''

git.commit()
        .setMessage("Master commit")
        .setAllowEmpty(true)
        .call()

git.checkout()
        .setCreateBranch(true)
        .setName("develop")
        .call()

git.add()
        .addFilepattern(".")
        .call()

git.commit()
        .setMessage("Commit 1")
        .call()

git.checkout()
        .setCreateBranch(true)
        .setName("feature/branch")
        .call()

Files.move(new File((File) basedir, "Sample.new.java").toPath(),
        new File((File) basedir, sampleFilePath).toPath(),
        StandardCopyOption.REPLACE_EXISTING)

git.add()
        .addFilepattern(sampleFilePath)
        .call()

git.commit()
        .setMessage("Commit 2")
        .call()

return true