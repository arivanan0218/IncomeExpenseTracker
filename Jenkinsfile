pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK17'
    }

    environment {
        DOCKER_HUB_CREDS = credentials('dockerhub-credentials')
        DOCKER_IMAGE_NAME = 'arivanan/spring-boot-backend'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
                sh "docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${DOCKER_IMAGE_NAME}:latest"
            }
        }

        stage('Push Docker Image') {
            steps {
                sh "echo ${DOCKER_HUB_CREDS_PSW} | docker login -u ${DOCKER_HUB_CREDS_USR} --password-stdin"
                sh "docker push ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                sh "docker push ${DOCKER_IMAGE_NAME}:latest"
            }
        }

        stage('Deploy to AWS') {
            steps {
                withAWS(credentials: 'aws-credentials', region: 'us-east-1') {
                    sh '''
                        aws ssm send-command \
                        --document-name "AWS-RunShellScript" \
                        --targets "Key=tag:Name,Values=production-server" \
                        --parameters "commands=[\
                            'docker pull ${DOCKER_IMAGE_NAME}:latest', \
                            'docker stop backend-container || true', \
                            'docker rm backend-container || true', \
                            'docker run -d --name backend-container -p 8081:8081 ${DOCKER_IMAGE_NAME}:latest' \
                        ]"
                    '''
                }
            }
        }
    }

    post {
        always {
            sh "docker rmi ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} || true"
            sh "docker rmi ${DOCKER_IMAGE_NAME}:latest || true"
        }
    }
}