// pipeline {
//     agent any
//     tools {
//         maven 'maven_3_9_9'
//     }
//     stages {
//         stage('Build Maven') {
//             steps {
//                 // Checkout code from GitHub repository
//                 checkout scmGit(branches: [[name: '*/main']], userRemoteConfigs: [[url: 'https://github.com/arivanan0218/IncomeExpenseTracker']])
//
//                 // Run Maven clean install
//                 bat 'mvn clean install'
//             }
//         }
//
//         stage('Build Docker image') {
//             steps {
//                 script {
//                     // Build Docker image
//                     bat 'docker build -t arivanan2001/income-expense .'
//                 }
//             }
//         }
//
//         stage('Push images to Hub') {
//             steps {
//                 script {
//                     // Login to Docker Hub using credentials stored in Jenkins
//                     withCredentials([string(credentialsId: 'dockerhubpwd', variable: 'dockerhubpwd')]) {
//                         bat 'echo %dockerhubpwd% | docker login -u arivanan2001 --password-stdin'
//
//                         // Push the Docker image to Docker Hub
//                         bat 'docker push arivanan2001/income-expense'
//                     }
//                      bat 'docker logout'
//                 }
//             }
//         }
//
//         stage ('Deploy to k8s'){
//             steps{
//                 script{
//                     kubernetesDeploy (configs: 'deploymentservice.yaml', kubeconfigId: 'k8pwd')
//                 }
//             }
//         }
//     }
// }


pipeline {
    agent any
    tools {
        maven 'maven_3_9_9'
    }
    environment {
        IMAGE_NAME = 'arivanan2001/income-expense'
        DOCKER_CREDENTIALS_ID = 'dockerhubpwd'
        KUBECONFIG_CREDENTIALS_ID = 'k8pwd'
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], userRemoteConfigs: [[url: 'https://github.com/arivanan0218/IncomeExpenseTracker']])
            }
        }

        stage('Build Maven') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Security Scan') {
            steps {
                bat 'mvn org.owasp:dependency-check-maven:check'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    bat "docker build -t ${IMAGE_NAME} ."
                }
            }
        }

        stage('Push Image to Docker Hub') {
            steps {
                script {
                    withCredentials([string(credentialsId: DOCKER_CREDENTIALS_ID, variable: 'dockerhubpwd')]) {
                        bat 'echo %dockerhubpwd% | docker login -u arivanan2001 --password-stdin'
                        bat "docker push ${IMAGE_NAME}"
                        bat 'docker logout'
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    kubernetesDeploy(configs: 'deploymentservice.yaml', kubeconfigId: KUBECONFIG_CREDENTIALS_ID)
                }
            }
        }
    }
}
