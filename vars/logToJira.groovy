#!/usr/bin/groovy

def call(String operation, String environment) {
  node() {
    stage('Notify Developers') {
      def changelogContext
      def commit = checkout scm
      def jira_url = 'https://jira.naebers.me'
  
      withCredentials([usernamePassword(credentialsId: 'jira.jenkins.login', passwordVariable: 'pass', usernameVariable: 'user')]) {
          changelogContext = gitChangelog(
              from: [type: 'REF', value: env.BRANCH_NAME], 
              jira: [
                  issuePattern: '\\b[a-zA-Z]([a-zA-Z]+)-([0-9]+)\\b',
                  server: jira_url,
                  username: user,
                  password: pass 
              ], 
              returnType: 'CONTEXT', 
              to: [type: 'COMMIT', value: commit.GIT_COMMIT]
          )
      }

      changelogContext.issues.each { issue ->
        if (issue.name == 'Jira') {
          jiraComment body: "${operation} to ${environment} in build ${currentBuild.displayName} of ${env.JOB_NAME}", issueKey: issue.issue
        }
      }
    }
  }
}