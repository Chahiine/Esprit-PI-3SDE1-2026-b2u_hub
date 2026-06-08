pipeline {
    agent any

    environment {
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'b2u-hub'
        BACKEND_IMAGE = 'b2u-hub-backend'
        FRONTEND_IMAGE = 'b2u-hub-frontend'
        DOCKER_BUILDKIT = '1'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Backend') {
            steps {
                sh './mvnw clean verify -DskipTests=false -B'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        ./mvnw sonar:sonar \
                          -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.token=\$SONAR_TOKEN \
                          -B
                    """
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build Backend') {
            steps {
                script {
                    def tag = "${BACKEND_IMAGE}:${env.BUILD_NUMBER}"
                    sh "docker build -f Dockerfile.backend -t ${tag} -t ${BACKEND_IMAGE}:latest ."
                }
            }
        }

        stage('Docker Build Frontend') {
            steps {
                script {
                    def tag = "${FRONTEND_IMAGE}:${env.BUILD_NUMBER}"
                    sh "docker build -f Dockerfile -t ${tag} -t ${FRONTEND_IMAGE}:latest ./frontend"
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline OK: SonarQube + Docker images buildes.'
        }
        failure {
            echo 'Pipeline en echec. Verifiez les logs Jenkins, SonarQube et Docker.'
        }
        always {
            cleanWs()
        }
    }
}
