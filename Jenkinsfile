@Library("shared-library@master") _

def isValid = true;

pipeline {
	agent {
		label 'macos'
	}

	environment {
    	PATH = "/Applications/Docker.app/Contents/Resources/bin:${env.PATH}"
    	USER = 'rwdevops999'
    	IMAGE = 'topbackend'
  	}
  
	stages {
		stage("info") {
			steps {
			    sh 'mvn -v'
			    sh 'docker -v'
			}
		}

		stage("init") {
			steps {
				build job: 'DockerCompose', parameters: [string(name: 'COMPOSE', value: 'DOWN' )], wait: true 
			}
		}
		

		stage("clean") {
			steps {
				sh 'mvn clean'	    
			}
		}

		stage("build") {
			steps {
				sh 'export spring_profiles_active=docker'	    
				sh 'mvn install -DskipTests'	    
			}
			
			post {
			    failure {
    			    script {
    			        isValid = false
    			    }
    			}
			}

		}
		
		stage("test") {
			when {
			    expression {
    			   isValid
    			}
			}

			steps {
				sh 'export spring_profiles_active=test'	    
				sh 'mvn test'	    
			}
			
			post {
			    success {
			    junit skipPublishingChecks: true, testResults: '**/test-results/TEST-*.xml'
//			        junit testResults: '**/test-results/**/*.xml', skipPublishingChecks: true
			    }

				failure {
				    script {
    				    isValid = false
    				}
				}
			}
		}

		stage("package") {
			when {
			    expression {
    			   isValid
    			}
			}

			environment {
			    DOCKERHUB_ACCESSKEY = credentials('DockerHubUserPassword')
			    KEYCHAIN_PSW = credentials('keychain')
			}
			
			steps {
				sh '''
					security unlock-keychain -p ${KEYCHAIN_PSW}
					docker login -u ${DOCKERHUB_ACCESSKEY_USR} -p ${DOCKERHUB_ACCESSKEY_PSW}
					docker build . -t ${IMAGE}
				'''
			}
			
			post {
			    failure {
			        script {
        			    isValid = false
        			}
			    }

			}

		}

		stage("publish") {
			when {
			    expression {
    			   isValid
    			}
			}

			steps {
				sh '''
					docker logout registry-1.docker.io
					docker tag ${IMAGE} ${USER}/${IMAGE}
					docker push ${USER}/${IMAGE}
				'''
			}

			post {
				success {
				    sh '''
				    	docker rmi -f ${IMAGE}:latest
				    	docker rmi -f ${USER}/${IMAGE}:latest
				    '''
				}

				failure {
				    script {
    				    isValid = false
    				}
				}
			}
		}

		stage("finalize") {
			when {
		    	expression {
		        	isValid
    			}
		  	}

		  	steps {
				build job: 'DockerCompose', parameters: [string(name: 'COMPOSE', value: 'UP' )], wait: true 
		  	}
		}
	}
	
	post {
	  success {
	    mailTo(to: 'rudi.welter@gmail.com', attachLog: true)
	  }

	  failure {
	    mailTo(to: 'rudi.welter@gmail.com', attachLog: true)
	  }
	}	
}