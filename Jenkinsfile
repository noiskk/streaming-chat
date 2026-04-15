pipeline {
    agent any

    environment {
        SONAR_SERVER_NAME = 'sonarqube'
        IMAGE_NAME = 'sw_team_1-backend'
        IMAGE_TAG = "${BUILD_NUMBER}"
        GEMINI_API_KEY = credentials('gemini-api-key')
    }

    stages {
        stage('Git Clone') {
            steps {
                git branch: 'main', url: 'https://github.com/noiskk/streaming-chat.git'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('backend') {
                    withSonarQubeEnv("${SONAR_SERVER_NAME}") {
                        sh 'chmod +x gradlew'
                        sh './gradlew sonar'
                    }
                }
            }
        }

        stage('Gradle Build') {
            steps {
                dir('backend') {
                    sh './gradlew clean build -x test'
                }
            }
        }

        stage('Docker Build') {
            steps {
                // 백엔드 이미지 빌드
                dir('backend') {
                    sh """
                        docker build -t sw_team_1-backend:${IMAGE_TAG} .
                        docker tag sw_team_1-backend:${IMAGE_TAG} sw_team_1-backend:latest
                    """
                }
                // 프론트엔드 이미지 빌드
                dir('frontend') {
                    sh """
                        docker build -t sw_team_1-frontend:${IMAGE_TAG} .
                        docker tag sw_team_1-frontend:${IMAGE_TAG} sw_team_1-frontend:latest
                    """
                }
            }
        }

        stage('Blue-Green Deploy') {
            steps {
                sh 'chmod +x deploy.sh'
                sh "./deploy.sh ${IMAGE_TAG}"
            }
        }
    }

    post {
        success {
            echo '블루-그린 배포 성공!'
        }
        failure {
            echo '빌드 또는 배포 중 에러가 발생했습니다.'
        }
    }
}
