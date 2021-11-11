
import org.eclipse.jgit.api.Git

def git = Git.init().setDirectory((File) basedir).call()


git.commit()
        .setMessage("Master commit")
        .setAllowEmpty(true)
        .call()

git.checkout()
        .setCreateBranch(true)
        .setName("develop")
        .call()

return true