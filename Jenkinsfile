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

        stage('Push images to Hub') {
            steps {
                script {
                    // Login to Docker Hub using credentials stored in Jenkins
                    withCredentials([string(credentialsId: 'dockerhubpwd', variable: 'dockerhubpwd')]) {
                        bat 'echo %dockerhubpwd% | docker login -u arivanan2001 --password-stdin'

                        // Push the Docker image to Docker Hub
                        bat 'docker push arivanan2001/income-expense'
                    }
                     bat 'docker logout'
                }
            }
        }

        stage ('Deploy to k8s'){
            steps{
                script{
                    kubernetesDeploy (configs: 'deploymentservice.yaml', kubeconfigId: 'k8pwd')
                }
            }
        }
    }

    post {
        success {
            emailext (
                to: 'arivuarivanan7@gmail.com, vamathevanarivanan@gmail.com',
                subject: 'Jenkins Build SUCCESS - IncomeExpenseTracker',
                body: 'The Jenkins pipeline for IncomeExpenseTracker has completed successfully.'
            )
        }
        failure {
            emailext (
                to: 'arivuarivanan7@gmail.com, vamathevanarivanan@gmail.com',
                subject: 'Jenkins Build FAILED - IncomeExpenseTracker',
                body: 'The Jenkins pipeline for IncomeExpenseTracker has failed. Please check the logs in Jenkins.'
            )
        }
    }
}
