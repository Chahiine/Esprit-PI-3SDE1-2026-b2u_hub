// Ancien pipeline tout-en-un (job b2u-hub-pipeline) — corrige le chemin Dockerfile frontend
pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    environment {
        GIT_REPO = 'https://github.com/Chahiine/Esprit-PI-3SDE1-2026-b2u_hub.git'
        SONAR_PROJECT_KEY = 'b2u-hub'
        SONAR_SERVER = 'SonarQube'
        SONAR_TOKEN_ID = 'sonar-token'
        BACKEND_IMAGE = 'b2u-hub-backend'
        FRONTEND_IMAGE = 'b2u-hub-frontend'
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                sh 'git clone --branch main --depth 1 ' + GIT_REPO + ' .'
            }
        }

        stage('Build & Test Backend') {
            steps {
                sh 'chmod +x mvnw && ./mvnw clean verify -DskipTests=false -B'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(SONAR_SERVER) {
                    withCredentials([string(credentialsId: SONAR_TOKEN_ID, variable: 'SONAR_TOKEN')]) {
                        sh './mvnw sonar:sonar -Dsonar.projectKey=' + SONAR_PROJECT_KEY + ' -Dsonar.token=$SONAR_TOKEN -B'
                    }
                }
            }
        }

        stage('Docker Build Backend') {
            steps {
                sh 'docker build -f Dockerfile.backend -t ' + BACKEND_IMAGE + ':' + BUILD_NUMBER + ' -t ' + BACKEND_IMAGE + ':latest .'
            }
        }

        stage('Docker Build Frontend') {
            steps {
                sh 'docker build -f frontend/Dockerfile -t ' + FRONTEND_IMAGE + ':' + BUILD_NUMBER + ' -t ' + FRONTEND_IMAGE + ':latest ./frontend'
            }
        }
    }

    post {
        success {
            echo 'Pipeline OK.'
        }
        failure {
            echo 'Pipeline en echec.'
        }
    }
}
