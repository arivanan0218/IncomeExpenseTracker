pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'production'], description: 'Select deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8081', description: 'Port for the application to run on EC2')
        string(name: 'POSTGRESQL_PORT', defaultValue: '3306', description: 'Port for MySQL to run on EC2')
    }

    environment {
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE = "arivanan/myapp-backend"
        EC2_HOST = credentials('ec2-host')
        EC2_USER = 'ubuntu'
        DEPLOY_ENV = "${params.DEPLOY_ENV ?: 'staging'}"
        SERVER_PORT = "${params.SERVER_PORT}"
        MYSQL_PORT = "${params.MYSQL_PORT}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package -DskipTests --no-transfer-progress'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
                    sh "docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest"
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                sh 'echo $DOCKER_CREDENTIALS_PSW | docker login -u $DOCKER_CREDENTIALS_USR --password-stdin'
                sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                sh "docker push ${DOCKER_IMAGE}:latest"
            }
        }

        stage('Deploy to EC2') {
            steps {
                withEnv([
                    "REMOTE_USER=${EC2_USER}",
                    "REMOTE_HOST=${EC2_HOST}",
                    "DOCKER_USERNAME=${DOCKER_CREDENTIALS_USR}",
                    "DOCKER_PASSWORD=${DOCKER_CREDENTIALS_PSW}"
                ]) {
                    sshagent(['ec2-ssh-key']) {
                        sh '''
                            set -e  # Exit on any error

                            echo "Preparing deployment on ${REMOTE_USER}@${REMOTE_HOST}..."

                            # First check and clean existing deployment directory if needed
                            echo "Checking and cleaning existing directories if needed..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "sudo rm -rf ~/app-deployment || true"

                            # Create remote directory structure with proper permissions
                            echo "Creating remote directories with proper permissions..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "mkdir -p ~/app-deployment"

                            # Ensure the ubuntu user owns all these directories
                            echo "Setting ownership of remote directories..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "sudo chown -R $REMOTE_USER:$REMOTE_USER ~/app-deployment"

                            # Ensure proper permissions on remote directories
                            echo "Setting permissions on remote directories..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "chmod -R 755 ~/app-deployment"

                            # List directories to verify
                            echo "Verifying remote directories..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "ls -la ~/app-deployment/"

                            # Copy main config files
                            echo "Copying docker-compose.yml"
                            scp -o StrictHostKeyChecking=no docker-compose.yml $REMOTE_USER@$REMOTE_HOST:~/app-deployment/

                            # Deploy the application
                            echo "Deploying application..."
                            ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST bash << 'EOF'
cd ~/app-deployment
echo "Current directory: $(pwd)"
echo "Logging into Docker Hub..."
echo "$DOCKER_PASSWORD" | sudo docker login --username "$DOCKER_USERNAME" --password-stdin

echo "Stopping existing services..."
sudo docker-compose down --remove-orphans || true

echo "Cleaning up any stale containers..."
sudo docker ps -aq | xargs sudo docker rm -f 2>/dev/null || true
sudo docker network prune -f || true

echo "Pulling latest images..."
sudo docker-compose pull

echo "Starting services..."
sudo docker-compose up -d

echo "Checking if services started successfully..."
if ! sudo docker-compose ps | grep -q "Up"; then
    echo "Containers failed to start properly"
    sudo docker-compose logs
    exit 1
fi

echo "Deployment completed successfully!"
EOF
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Clean up Docker resources
                sh 'docker logout || true'
                sh 'docker system prune -f || true'

                // Remove sensitive files
                sh '''

                    rm -f get-docker.sh || true
                '''

                cleanWs()
            }
        }
        success {
            echo "Successfully deployed to ${DEPLOY_ENV} environment at ${EC2_HOST}"
        }
        failure {
            echo "Deployment to ${DEPLOY_ENV} failed"
        }
    }
}