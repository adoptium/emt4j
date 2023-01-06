pipeline {
  agent any
  stages {
    stage('verify') {
      steps {
        sh 'mvn clean verify -Ptest'
      }
    }
  }
}
