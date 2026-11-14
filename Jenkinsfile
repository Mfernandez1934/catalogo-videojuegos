// Pipeline declarativo - Catalogo de Videojuegos (Entregable 4 - CI/CD)
// Funciona en Jenkins sobre Windows, Linux o macOS (detecta el sistema con isUnix()).

// Ejecuta un comando de Maven con sh (Linux/macOS) o bat (Windows)
def mvn(String args) {
    if (isUnix()) {
        sh "mvn -B ${args}"
    } else {
        bat "mvn -B ${args}"
    }
}

pipeline {
    agent any

    tools {
        // Nombre de la instalacion de Maven en "Manage Jenkins > Tools"
        maven 'Maven3'
    }

    triggers {
        // Revisa GitHub cada minuto y ejecuta el pipeline si hay commits nuevos
        pollSCM('* * * * *')
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        APP_PORT = '8081'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                mvn('clean compile')
            }
        }

        stage('Pruebas unitarias') {
            steps {
                mvn('test')
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Pruebas de integracion') {
            steps {
                // -DskipUTs=true evita repetir las unitarias; Failsafe corre las clases *IT
                mvn('verify -DskipUTs=true')
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                archiveArtifacts artifacts: 'web/target/catalogo-web.jar', fingerprint: true
            }
        }

        stage('Deploy a produccion') {
            steps {
                // dontKillMe: evita que Jenkins mate la app cuando termina el build
                withEnv(['JENKINS_NODE_COOKIE=dontKillMe', 'BUILD_ID=dontKillMe']) {
                    script {
                        // Carpeta del ambiente simulado de produccion. Se puede cambiar
                        // definiendo PROD_DIR en "Manage Jenkins > System > Global properties".
                        if (isUnix()) {
                            def prodDir = env.PROD_DIR ?: "${env.HOME}/prod/catalogo"
                            sh "bash scripts/deploy.sh '${prodDir}' ${env.APP_PORT}"
                        } else {
                            def prodDir = env.PROD_DIR ?: 'C:\\prod\\catalogo'
                            powershell "& .\\scripts\\deploy.ps1 -ProdDir '${prodDir}' -Port ${env.APP_PORT}"
                        }
                    }
                }
            }
        }

        stage('Health check') {
            steps {
                script {
                    if (isUnix()) {
                        sh "bash scripts/healthcheck.sh ${env.APP_PORT}"
                    } else {
                        powershell "& .\\scripts\\healthcheck.ps1 -Port ${env.APP_PORT}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "OK: version desplegada en produccion -> http://localhost:${env.APP_PORT}"
        }
        failure {
            echo 'FALLO: el pipeline se detuvo. Produccion NO se modifico (sigue la version anterior).'
        }
    }
}
