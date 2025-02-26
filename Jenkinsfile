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
//
//                     }
//                      bat 'docker logout'
//                 }
//             }
//         }
//
//         stage ('Deploy to k8s'){
//             steps{
//                 script{
//                     kubernetesDeploy (configs: 'deploymentservice.yaml', kubeconfigId: 'k8ss')
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
        DOCKERHUB_CREDENTIALS = credentials('dockerhubpwd')
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], userRemoteConfigs: [[url: 'https://github.com/arivanan0218/IncomeExpenseTracker']])
            }
        }

        stage('Check Build Errors') {
            steps {
                script {
                    bat 'mvn validate'
                }
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    bat 'mvn test'
                }
            }
        }

        stage('Build Maven') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    bat 'docker build -t arivanan2001/income-expense .'
                }
            }
        }

        stage('Push Image to Docker Hub') {
            steps {
                script {
                    bat "echo %DOCKERHUB_CREDENTIALS% | docker login -u arivanan2001 --password-stdin"
                    bat 'docker push arivanan2001/income-expense'
                    bat 'docker logout'
                }
            }
        }

//         stage('Provision Infrastructure with Terraform') {
//             steps {
//                 script {
//                     bat 'terraform init'
//                     bat 'terraform apply -auto-approve'
//                 }
//             }
//         }

//         stage('Configure Environment with Ansible') {
//             steps {
//                 script {
//                     bat 'ansible-playbook -i inventory setup.yml'
//                 }
//             }
//         }

//         stage('Deploy to Kubernetes') {
//             steps {
//                 script {
//                     kubernetesDeploy(configs: 'deploymentservice.yaml', kubeconfigId: 'k8ss')
//                 }
//             }
//         }
    }
}
