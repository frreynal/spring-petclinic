pipeline {
    agent any
    triggers {pollSCM('* * * * *')}
    stages {
        stage('Checkout') {
            steps {
                // Get some code from a GitHub repository
                git branch: 'main',
                        credentialsId: 'bcbc8f20-45f3-4305-988a-1bcceca9c308',
                        url: 'https://github.com/frreynal/spring-petclinic.git'

            }
        }

        stage('Format') {
            steps {
                withMaven(maven: 'maven-perso') {
                    bat 'mvn spring-javaformat:apply'
                }
            }
        }
        stage('Build') {
            steps {
                withMaven(maven: 'maven-perso') {
                    bat 'mvn clean package'
                }
            }
        }

        // To run Maven on a Windows agent, use
        // bat "mvn -Dmaven.test.failure.ignore=true clean package"
    }

    post {
        // If Maven was able to run the tests, even if some of the test
        // failed, record the test results and archive the jar file.
        always {
            junit '**/target/surefire-reports/TEST-*.xml'
            emailext attachLog: true,
                    body: ' Please go to ${BUILD_URL} and verify the build',
                    compressLog: true,
                    recipientProviders: [requestor(), upstreamDevelopers()],
                    subject: 'Job \'${JOB_NAME}\' (${BUILD_NUMBER}) is waiting for input'
        }
        success {
            archiveArtifacts 'target/*.jar'
        }

    }
}
