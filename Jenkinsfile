pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE = "employee-service:${env.BUILD_NUMBER}"
    }

    stages {
        stage('Build and test') {
            steps {
                sh './mvnw -B verify'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Docker image') {
            when {
                anyOf { branch 'develop'; branch 'test'; branch 'main' }
            }
            steps {
                sh "docker build -t ${IMAGE} ."
            }
        }

        stage('Deploy') {
            when {
                anyOf { branch 'test'; branch 'main' }
            }
            steps {
                echo "Deploy ${IMAGE} to ${env.BRANCH_NAME == 'main' ? 'production' : 'testing'}"
            }
        }
    }
}
