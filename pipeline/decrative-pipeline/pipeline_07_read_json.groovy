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
                script{
                    sh 'curl -f -o date.json https://worldtimeapi.org/api/timezone/Etc/UTC'
                    json = readJSON file: 'date.json'
                    echo "TODAY is ${json.date}"
                }
            }
        }
    }
}
