/* Disable concurrent builds to prevent concurrent Maven builds in the same workspace. */
properties([disableConcurrentBuilds()])

timeout(time: 3, unit: 'HOURS') {

    /* Maven Options
     *             --batch-mode : recommended in CI to inform maven to not run in interactive mode (less logs).
     *                       -V : strongly recommended in CI, will display the JDK and Maven versions in use.
     *                       -U : force maven to update snapshots each time (default : once an hour, makes no sense in CI).
     * -Dsurefire.useFile=false : useful in CI. Displays test errors in the logs (instead of having to crawl the workspace to see the cause).
     */
    def mavenOptions = "--batch-mode -V -U -e -Dsurefire.useFile=false"

    /* The JVM options for Maven. TieredCompilation should improve JVM startup speed (see https://zeroturnaround.com/rebellabs/your-maven-build-is-slow-speed-it-up/). */
    final String mavenJvmOptions = '-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Dorg.jenkinsci.plugins.pipeline.maven.eventspy.JenkinsMavenEventSpy.disabled=true'

    /* Maven settings file configured in Jenkins with server credentials. */
    def mavenSettings = '6477bc0c-502b-4247-ac2e-dd8f0e62d61d'

    /* Name of centrally configured Maven installation in Jenkins. */
    def mavenToolName = 'Maven 3.5.2'

    /* Name of centrally configured JDK installation in Jenkins. */
    def jdkToolName = 'Java 8 JDK (Latest)'

    /* Name of branch being built. */
    def branch = env['BRANCH_NAME']

    /** Git commit hash. */
    def commit = null

    /* Name of job (URL encoded by Jenkins, so we decode it). */
    def jobName = URLDecoder.decode(env['JOB_NAME'], "UTF-8")

    /* Name of build. */
    def buildName = jobName + '#' + env['BUILD_NUMBER']

    /* Whether this is a release build. */
    boolean releaseBuild = false

    /** The version being built. */
    String version = null

    /* Recipients to send success/failure/release notifications to (comma separated list of email addresses). */
    List notificationRecipients = ["james.harrison@cognitran.com"]
    notificationRecipients.addAll(Arrays.asList(((String) emailextrecipients([
            [$class: 'CulpritsRecipientProvider'],
            [$class: 'DevelopersRecipientProvider'],
            [$class: 'RequesterRecipientProvider']
    ])).split('\\s+')))
    notificationRecipients.unique()

    // Run on any node with itis (or cloud-build) and linux labels.
    node('(itis || cloud-build) && linux') {
        try {
            stage('Checkout') {
                // Git checkout the branch.
                checkout scm

                // Set build description to commit hash for convenience.
                commit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                currentBuild.description = commit

                // Make sure that committer and author are included in notification emails.
                notificationRecipients.add(sh(returnStdout: true, script: 'git --no-pager log -1 --pretty=format:"%ae"').trim())
                notificationRecipients.add(sh(returnStdout: true, script: 'git --no-pager log -1 --pretty=format:"%ce"').trim())
                notificationRecipients.unique()
                print "Notification Recipients: " + notificationRecipients.join(" ")
            }

            stage ('Verify') {
                releaseBuild = branch != "develop" &&
                        !branch.startsWith("feature/") &&
                        !branch.startsWith("hotfix/") &&
                        !branch.startsWith("release/") &&
                        !branch.startsWith("wip/") &&
                        !branch.endsWith("-wip")

                withMaven(maven: mavenToolName, globalMavenSettingsConfig: mavenSettings, jdk: jdkToolName,
                        mavenOpts: "${mavenJvmOptions} -Xmx64m " +
                                "-Dorg.slf4j.simpleLogger.defaultLogLevel=ERROR " +
                                "-Dorg.slf4j.simpleLogger.log.org.apache.maven.plugins.help=INFO") {
                    version = sh(script: "mvn ${mavenOptions} help:evaluate -Dexpression=project.version | grep -v '^\\[' | grep -v 'JenkinsMavenEventSpy' | tail -1",
                            returnStdout: true).trim()
                }

                echo "Branch Name: ${branch}\nVersion: ${version}"

                if (releaseBuild && version.endsWith('-SNAPSHOT')) {
                    error('Release builds must not have a SNAPSHOT version. If this is not a release build, please ensure that the branch name starts with "wip/", "feature/", "release/", "hotfix/", or "develop". POM version is "' + version + '".')
                } else if (!releaseBuild && !version.endsWith('-SNAPSHOT')) {
                    error('Non-release builds must have a SNAPSHOT version. Please make sure that you have based your branch on a "Work in Progress" ("develop", "release/*", "hotfix/*", or "wip/*") branch and that the POM version ends with "-SNAPSHOT". POM version is "' + version + '".')
                }
            }

            stage('Maven Build & Deploy') {
                // Main part of build included deployment of artifacts.
                withMaven(maven: mavenToolName, globalMavenSettingsConfig: mavenSettings, jdk: jdkToolName, mavenOpts: mavenJvmOptions) {
                    sh "mvn ${mavenOptions} clean deploy"
                }
            }

            if (releaseBuild) {
                final List releaseNotificationRecipients = []
                releaseNotificationRecipients.addAll(notificationRecipients)
                // releaseNotificationRecipients.add('products.developers@cognitran.com')
                print "Release Notification Recipients: " + releaseNotificationRecipients.join(" ")

                // Send release notification with link to release notes.
                emailext(subject: "Release Notification: ITIS Maven Plugin ${version}",
                        body: """<h2>ITIS Maven Plugin ${version}</h2>
                                  <p>ITIS Maven Plugin ${version} has been released and is now available from the Cognitran Maven repository.</p>
                                  <p>Documentation for this version is at https://bitbucket.org/cognitranlimited/itis-maven-plugin/src/master/README.md</p>""",
                        mimeType: "text/html",
                        to: releaseNotificationRecipients.join(" "))
            }
        }
        catch (e) {
            // Send failure notification.
            emailext(subject: "BUILD FAILED: ${buildName}",
                    body: """<h2>BUILD FAILED: ${buildName}</h2>
                           <p>Check console output at <a href='${env.BUILD_URL}'>${buildName}</a>.</p>""",
                    mimeType: "text/html",
                    to: notificationRecipients.join(" "))

            // Rethrow exception to mark build as failure.
            throw e
        }

        // Send build success notification.
        emailext(subject: "BUILD SUCCESS: ${buildName}",
                body: """<h2>BUILD SUCCESS: ${buildName}</h2>
                                  <p>Check console output at <a href='${env.BUILD_URL}'>${buildName}</a>.</p>""",
                mimeType: "text/html",
                to: notificationRecipients.join(" "))
    }
}
