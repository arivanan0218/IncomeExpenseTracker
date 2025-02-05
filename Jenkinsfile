pipeline {
    agent any
    tools {
        maven 'maven_3_9_9'
    }
    stages {
        stage('Build Maven') {
            steps {
                // Checkout code from GitHub repository
                checkout scmGit(branches: [[name: '*/main']], userRemoteConfigs: [[url: 'https://github.com/arivanan0218/IncomeExpenseTracker']])

                // Run Maven clean install
                bat 'mvn clean install'
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    // Build Docker image
                    bat 'docker build -t arivanan2001/income-expense .'
                }
            }
        }

        stage('Build PostgreSQL Docker image') {
            steps {
                script {
                    // Build PostgreSQL Docker image (if you have a custom Dockerfile for PostgreSQL)
                    bat 'docker build -t arivanan2001/postgres-db ./postgres'
                }
            }
        }

        stage('Push images to Hub') {
            steps {
                script {
                    // Login to Docker Hub using credentials stored in Jenkins
                    withCredentials([string(credentialsId: 'dockerhubpwd', variable: 'dockerhubpwd')]) {
                        bat 'echo %dockerhubpwd% | docker login -u arivanan2001 --password-stdin'

                        // Push the Docker image to Docker Hub
                        bat 'docker push arivanan2001/income-expense'

                        // Push the PostgreSQL Docker image to Docker Hub
                        bat 'docker push arivanan2001/postgres-db'
                    }
                     bat 'docker logout'
                }
            }
        }

        stage ('Deploy to k8s'){
            steps{
                script{
                    kubernetesDeploy (configs: 'deploymentservice.yaml', kubeconfigId: 'k8sconfigpwd')
                }
            }
        }
    }
}
