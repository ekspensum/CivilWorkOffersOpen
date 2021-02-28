pipeline {
    agent any

    tools {
        maven "M3"
    }

    stages {
        stage('Build') {
            steps {
                sh "mvn clean install -DskipTests"
            }
        }
        stage('Test') { 
            steps {
                sh "mvn test" 
            }
        }
        stage('Deploy') { 
            steps {
            	sh "docker start mysql-standalone"
                sh "docker build -t civilworkoffers ."
                sh "docker rm -f app-civilworkoffers"
                sh "docker run --network civilworkoffers-mysqldb --name app-civilworkoffers -p 8085:8080 -d civilworkoffers" 
            }
        }
    }
}

