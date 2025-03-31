pipeline {
    agent any

    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'production'], description: 'Select deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8081', description: 'Port for the application to run on EC2')
        string(name: 'MYSQL_PORT', defaultValue: '3306', description: 'Port for MySQL to run on EC2')
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

//         stage('Test') {
//             steps {
//                 withEnv([
//                     'SPRING_PROFILES_ACTIVE=test',
//                     'SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb',
//                     'SPRING_DATASOURCE_USERNAME=sa',
//                     'SPRING_DATASOURCE_PASSWORD=',
//                     'SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop'
//                 ]) {
//                     sh './mvnw test'
//                 }
//             }
//             post {
//                 always {
//                     junit '**/target/surefire-reports/*.xml'
//                 }
//             }
//         }

//         stage('Prepare .env File') {
//             steps {
//                 script {
//                     sh '''
//                         # Create .env file with secure permissions
//                         touch .env && chmod 600 .env
//
//                         cat > .env << EOL
// SPRING_APPLICATION_NAME=backend
// SERVER_PORT=${SERVER_PORT}
// MYSQL_PORT=${MYSQL_PORT}
// MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
// SPRING_JPA_HIBERNATE_DDL_AUTO=update
// SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/dental
// SPRING_DATASOURCE_USERNAME=${DB_CREDENTIALS_USR}
// SPRING_DATASOURCE_PASSWORD=${DB_CREDENTIALS_PSW}
// SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
// SPRING_JPA_SHOW_SQL=false
// APP_CORS_ALLOWED_ORIGINS=http://3.6.43.242,http://localhost:3000
// APP_RESET_PASSWORD_LINK=http://myapp.com/reset-password
// SPRING_APP_JWTSECRET=${JWT_SECRET}
// SPRING_APP_JWTEXPIRATIONMS=86400000
// SPRING_APP_JWTCOOKIENAME=dn-dental-clinic
// SPRING_MAIL_HOST=smtp.gmail.com
// SPRING_MAIL_PORT=587
// SPRING_MAIL_USERNAME=${MAIL_CREDENTIALS_USR}
// SPRING_MAIL_PASSWORD=${MAIL_CREDENTIALS_PSW}
// SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
// SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
// SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_REQUIRED=true
// SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTIONTIMEOUT=5000
// SPRING_MAIL_PROPERTIES_MAIL_SMTP_TIMEOUT=5000
// SPRING_MAIL_PROPERTIES_MAIL_SMTP_WRITETIMEOUT=5000
// AWS_ACCESSKEYID=${AWS_CREDENTIALS_USR}
// AWS_SECRETKEY=${AWS_CREDENTIALS_PSW}
// AWS_REGION=eu-north-1
// AWS_S3_BUCKET=patient-logbook-photos
// EOL
//
//                         # Verify file was created successfully
//                         if [ ! -f .env ]; then
//                             echo "Failed to create .env file"
//                             exit 1
//                         fi
//                     '''
//                 }
//             }
//         }
//
//         stage('Check Monitoring Directories') {
//             steps {
//                 script {
//                     sh '''
//                         # Verify that the required directories exist
//                         if [ ! -d "prometheus" ]; then
//                             echo "Error: prometheus directory not found in project"
//                             exit 1
//                         fi
//
//                         if [ ! -d "grafana/provisioning/dashboards" ]; then
//                             echo "Error: grafana/provisioning/dashboards directory not found in project"
//                             exit 1
//                         fi
//
//                         if [ ! -d "grafana/provisioning/datasources" ]; then
//                             echo "Error: grafana/provisioning/datasources directory not found in project"
//                             exit 1
//                         fi
//
//                         # Verify the directories contents
//                         ls -la prometheus/
//                         ls -la grafana/provisioning/dashboards/
//                         ls -la grafana/provisioning/datasources/
//                     '''
//                 }
//             }
//         }

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