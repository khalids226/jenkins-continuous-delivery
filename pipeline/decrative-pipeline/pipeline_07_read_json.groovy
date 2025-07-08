/**
* TOPICS
* - readJSON
*     - https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
*/

pipeline {

    agent any

    stages {
        stage('get current date') {
            steps {
                script {
                    // Fetch date data
                    sh 'curl -f -o date.json https://worldtimeapi.org/api/timezone/Etc/UTC'

                    // Safely declare JSON object
                    def json = readJSON file: 'date.json'

                    // Access the correct key
                    echo "TODAY is ${json.datetime}"
                }
            }
        }
    }
}
