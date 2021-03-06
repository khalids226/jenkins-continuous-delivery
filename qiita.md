# declarative pipeline とは

Jenkins pipelineは次の２つの構文をサポートしています。

* Scripted Pipeline
* Declarative Pipeline (Pipeline 2.5で導入)

Scripted Pipelineは柔軟な表現ができますがやや複雑でした。
Declarative Pipeline ではよりシンプルな記述が可能になりました。
そして、Declarative Pipeline では必要に応じてScripted Pipelineの柔軟な表現も行えるため、両者のメリットを共に享受することができる構文となっています。

本記事では実際にdeclarative pipelineでどのようなことができるのかを紹介していきたいと思います。
Jenkinsfileは[こちら](https://github.com/sakamaki-y123/jenkins-continuous-delivery)コミットしてありますので、併せて紹介をしていきます。
また関連するページもサンプルごとに随時紹介をしていきます。

ここで紹介するsampleを実行できるサーバーを立てていますので、実際に触ってください。

---
# サンプル集 - Sample pipelines

* 利用している実行環境
    * ubuntu 16.04
    * Jenkins 2.107.3
* gihub
    * https://github.com/sakamaki-y123/jenkins-continuous-delivery
* Sample jenkins
    * http://118.27.19.114:8080/job/decrative-pipeline/
 

## 1. hello world
まずはhello worldをやってみます。
**topics**
1. [pipeline](https://jenkins.io/doc/book/pipeline/syntax/#declarative-pipeline)
2. [agent](https://jenkins.io/doc/book/pipeline/syntax/#agent)
3. [stages](https://jenkins.io/doc/book/pipeline/syntax/#stages)
4. [stage](https://jenkins.io/doc/book/pipeline/syntax/#steps)
5. [steps](https://jenkins.io/doc/book/pipeline/syntax/#steps)
6. [echo](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#echo-print-message)

**sample code**

```hello_wold.groovy
pipeline {
    agent any
    stages {
        stage('hello') {
            steps {
                echo 'hello world'
            }
        }
    }
}
```
![01_hello_world.gif](https://qiita-image-store.s3.amazonaws.com/0/133430/c1998a7b-f084-1ecd-6001-d6d684bb7465.gif)

**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_01_hello_world.groovy)
*  [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/01_hello_world/)

---
## 2. sh step を使ってコマンドを実行する
sh stepを使ってlinuxのos情報を表示してみます。

**topics**
1. [sh](https://jenkins.io/doc/pipeline/steps/workflow-durable-task-step/#sh-shell-script)

**sample code**

```use_sh_step.groovy
pipeline {
    agent any
    stages {
        stage('show os information') {
            steps {
                sh 'cat /etc/os-release'
            }
        }
    }
}
```

**link**

*  [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_02_use_sh_step.groovy)
*  [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/02_use_sh_step/)

---
## 3. ファイルの書き込みをする
writeFile stepを使ってファイルの書き込みをやってみます。
書き込む内容はパラメータから受け取ります。

**topics**
1. [writeFile](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#writefile-write-file-to-workspace)

**sample code**

```write_file.groovy
pipeline {
    agent any
    stages {
        stage('write file') {
            steps {
                writeFile(file: "output.txt", text: "${OUTPUT_TEXT}")
            }
        }
    }
}
```
**link**
* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_03_write_file.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/03_write_file/)


---
## 4. ファイルを成果物として保存する。
書き込んだファイルをビルドの成果物として保存してみます。
このサンプルではファイル名をenvironment blockを使って定数として宣言してみます。

**topics**
1. [environment](https://jenkins.io/doc/book/pipeline/syntax/#environment)
2. [archiveArtifacts](https://jenkins.io/doc/pipeline/steps/core/#archiveartifacts-archive-the-artifacts)

**sample code**

```archive_artifacts.groovy
pipeline {

    agent any

    environment {
        fileName = "output.txt"
    }

    stages {

        stage('write file') {
            steps {
                writeFile(file: fileName, text: "${OUTPUT_TEXT}")
            }
        }

        stage('archive artifacts') {
            steps {
                archiveArtifacts fileName
            }
        }
    }
}

```

![04_archive_artifacts.gif](https://qiita-image-store.s3.amazonaws.com/0/133430/b7a9aa4e-c10b-10b5-2761-b72698bd1ffd.gif)

**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_04_archive_artifacts.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/04_archive_artifacts/)

---
## 5. buildの後にワークスペースをクリーンします。
ビルドが成功したらcleanWs stepを使ってworkspaceを一掃します。

**topics**
1. [post](https://jenkins.io/doc/book/pipeline/syntax/#post)
2. [cleanWs](https://jenkins.io/doc/pipeline/steps/ws-cleanup/#cleanws-delete-workspace-when-build-is-done)

**sample code**

```clean_workspace.groovy
pipeline {

    agent any

    environment {
        fileName = "output.txt"
    }

    stages {

        stage('write file') {
            steps {
                writeFile(file: fileName, text: "${OUTPUT_TEXT}")
            }
        }

        stage('archive artifacts') {
            steps {
                archiveArtifacts fileName
            }
        }
    }

    post {
        success {
            cleanWs()
        }
    }
}
```

**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_05_clean_workspace.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/05_clean_workspace/)

---
## 6.ほかのジョブから取得した成果物を表示する。
ほかのジョブの成果物として保存されているファイルを取得し、
そのファイル名を表示するサンプルです。
ファイル名を一つずつ表示させるためscriptブロックを使います。


**topics**
1. [parameters](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_06_copy_artifacts.groovy)
2. [deleteDir](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#code-deletedir-code-recursively-delete-the-current-directory-from-the-workspace)
3. [copyArtifacts](https://jenkins.io/doc/pipeline/steps/copyartifact/#code-copyartifacts-code-copy-artifacts-from-another-project)
4. [script](https://jenkins.io/doc/book/pipeline/syntax/#script)
5. [findFiles](https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#findfiles-find-files-in-the-workspace
*/)

**sample code**

```copy_artifacts.groovy
pipeline {

    agent any

    parameters {
        string(
            name: 'COPY_SOURCE_PROJECT',
            defaultValue: "",
            description: 'Name of source project for copying of artifact(s).'
        )
    }

    stages {

        stage('delete workspace') {
            steps {
                deleteDir()
            }
        }

        stage('copy artifacts') {
            steps {
                copyArtifacts(projectName: "${params.COPY_SOURCE_PROJECT}")
            }
        }

        stage('find files') {
            steps {
                script {
                    files = findFiles(glob: '*.*')
                    for (file in files) {
                        echo file.name
                    }
                }
            }
        }
    }
}
```
**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_06_copy_artifacts.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/06_copy_artifacts/)

---
## 7. json を読み込んで使ってみる
日付データをjsonテストのページから取得し、今日の日付を表示してみます。

**topics**
1. [readJSON](https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace)

**sample code**

```read_json.groovy
pipeline {

    agent any

    stages {
        stage('get current date') {
            steps {
                script {
                    sh 'curl -f -o date.json "http://date.jsontest.com"'
                    json = readJSON file: 'date.json'
                    echo "TODAY is ${json.date}"
                }
            }
        }
    }
}
```
**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_07_read_json.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/07_read_json/)

---
## 8. ほかのジョブを並列実行で呼び出す。
parallel step を使ってほかのジョブを並列実行するサンプルです。

**topics**
1. [parallel](https://jenkins.io/doc/book/pipeline/syntax/#parallel)
2. [build](https://jenkins.io/doc/pipeline/steps/pipeline-build-step/#code-build-code-build-a-job)

**sample code**

```parallel_step.groovy
pipeline {

    agent any

    stages {
        stage('parallel build') {
            steps {
                parallel(
                    "01_hello_world": {
                        build '01_hello_world'
                    },
                    "02_use_sh_step": {
                        build '02_use_sh_step'
                    },
                    "03_write_file": {
                        build(
                            job: '03_write_file',
                            parameters: [
                                text(name: 'OUTPUT_TEXT', value: 'hoge hoge')
                            ]
                        )
                    }
                )
            }
        }
    }
}
```
![08_parallel_step.gif](https://qiita-image-store.s3.amazonaws.com/0/133430/2cc42e3c-29f4-6fd5-10d6-ceb1d393a3ab.gif)


**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_08_parallel_step.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/08_parallel_step/)

---

## 9. agent にdockerを使う
agent docker image を指定してshステップを実行してみます。

**topics**
1. [agent docker](https://jenkins.io/doc/book/pipeline/docker/#execution-environment)

**sample code**

```agent_docker.groovy
pipeline {

    agent {
        docker { 
            image 'node:7-alpine' 
        }
    }

    stages {
        stage('version') {
            steps {
                sh 'node --version'
            }
        }
    }
}
```

**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_09_agent_docker.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/09_agent_docker/)

---

## 10. 一つのパイプラインで複数のdocker image をagentにする
一つのパイプラインの中で複数のdocker imageをagentにするためのサンプルです。
ポイントは最初に`agente none` と設定し、stage ブロックの中で改めて利用するagentを指定することです。
**topics**
1. [agent none](https://jenkins.io/doc/book/pipeline/syntax/#agent)
2. [using multiple docker containers](https://jenkins.io/doc/book/pipeline/docker/#using-multiple-containers)

**sample code**

```use_multiple_docker.groovy
pipeline {
    agent none

    stages {
        stage('Back-end') {
            agent {
                docker {
                    image 'maven:3-alpine'
                }
            }
            steps {
                sh 'mvn --version'
            }
        }

        stage('Front-end') {
            agent {
                docker {
                    image 'node:7-alpine'
                }
            }
            steps {
                sh 'node --version'
            }
        }
    }
}
```
**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_10_use_multiple_docker.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/10_use_multiple_docker/)

---

## 11. YAMLを使ってデータのやり取りをしてみる。
YAML形式のデータのやり取りをやってみました。
YAMLのデータを書き換えてファイルに書き出すサンプルです。

**topics**
1. [readYaml](https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#code-readyaml-code-read-yaml-from-files-in-the-workspace-or-text)
2. [writeYaml](https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#code-writeyaml-code-write-a-yaml-from-an-object)

**sample code**

```read_and_write_yaml.groovy
def SAMPLE_YAML = """\
name:
  first: ""
  last: ""
dates:
  birth: ""
"""

def datas

pipeline {
    agent any

    environment {
        fileName = "sample.yml"
    }

    stages {
        stage('read yaml') {
            steps {
                script{
                    datas = readYaml text: "${SAMPLE_YAML}"
                    echo "Name is ${datas.name.first} ${datas.name.last}"
                    echo "Birthday is ${datas.dates.birth}"
                }
            }
        }

        stage('write yaml') {
            steps {
                script{
                    datas.name.first = "Ichiro"
                    datas.name.last = "Sato"
                    datas.dates.birth = "1980-01-01"
                    echo "Name is ${datas.name.first} ${datas.name.last}"
                    echo "Birthday is ${datas.dates.birth}"
                    writeYaml file: fileName, data: datas
                }
            }
        }

        stage('archive sample.yml') {
            steps {
                 archiveArtifacts fileName
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
```
**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_11_read_and_write_yaml.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/11_read_and_write_yaml/)


---

## 12. input でユーザー入力を受け付ける
パイプラインの途中でユーザーからの入力を受け付けるサンプルです。
入力がされなかったときはタイムアウトするようにしています。

**topics**
1. [timeout](https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#code-timeout-code-enforce-time-limit)
2. [input](https://jenkins.io/doc/pipeline/steps/pipeline-input-step/#code-input-code-wait-for-interactive-input)

**sample code**

```input_request.groovy
def SAMPLE_YAML = """\
name:
  first: ""
  last: ""
dates:
  birth: ""
"""

def datas

pipeline {
    agent any

    environment {
        fileName = "sample.yml"
    }

    stages {
        stage('read sample yaml') {
            steps {
                script{
                    datas = readYaml text: "${SAMPLE_YAML}"
                }
            }
        }

        stage('input name') {
            steps {
                script{
                    timeout(time:3, unit:'MINUTES') {
                        inputName = input(
                            id: "inputName",
                            message: "Please input your name.",
                            parameters:[
                                string( name: 'first', defaultValue: '', description: 'Input your first name.'),
                                string( name: 'last', defaultValue: '', description: 'Input your last name.')
                            ]
                        )
                        datas.name.first = inputName.first
                        datas.name.last = inputName.last
                    }
                }
            }
        }

        stage('input birthday') {
            steps {
                script{
                    timeout(time:3, unit:'MINUTES') {
                        inputBirthday = input(
                            id: "inputBirthday",
                            message: "Please input your birth day.",
                            parameters:[
                                string( name: 'birth', defaultValue: 'yyyy-MM-dd', description: ''),
                            ]
                        )
                        datas.dates.birth = inputBirthday
                    }
                }
            }
        }

        stage('write yaml') {
            steps {
                script{
                    echo "Name is ${datas.name.first} ${datas.name.last}"
                    echo "Birthday is ${datas.dates.birth}"
                    writeYaml file: fileName, data: datas
                    archiveArtifacts fileName
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
```
![12_input_request.gif](https://qiita-image-store.s3.amazonaws.com/0/133430/6eed250e-597f-3a44-6a2d-1190bd949c75.gif)


**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_12_input_request.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/12_input_request/)

---


## 13. ステージの実行を条件ごとに制御する
when ブロックを使って、条件に応じてステージの実行を制御します。
* environment 特定の文字列を比較する際に使います。
* expression スクリプトの真偽値の判定に使います。

pipeline内でファンクションを使用してサンプルを作りました。

**topics**
1. [when](https://jenkins.io/doc/book/pipeline/syntax/#when)
2. [define function](https://www.cakesolutions.net/teamblogs/jenkins2-pipelines-101)

**sample code**

```conditional_run_stage.groovy
def boolean skipStage( String startStageNo, String stageNo ){
    if( startStageNo.toInteger() <= stageNo.toInteger() ){
        return false
    } else {
        return true
    }
}

pipeline {
    agent any

    stages {
        stage('stage1') {

            when {
                environment name: 'START_STAGE_NO', value: '1'
            }

            steps {
                echo "run stage 1"
            }
        }

        stage('stage2') {

            when{
                not{
                    expression {
                        return skipStage( START_STAGE_NO, "2" )
                    }
                }
            }
            
            steps {
                echo "run stage 2"
            }
        }
    }
}
```
![13_conditional_run_stage.gif](https://qiita-image-store.s3.amazonaws.com/0/133430/308abf92-4c2f-76d8-1ef8-1f94a44e8167.gif)


**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_13_conditional_run_stage.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/13_conditional_run_stage/)

---

## 14. gitからansible-playbookのスクリプトを取得しDockerContainerを使って実行する
ansible-playbook をdocker を使ってhello worldするサンプルです。

1. https://github.com/chusiang/helloworld.ansible.role からansible-playbookのsampleを取得。
2. williamyeh/ansible:alpine3 のdocker image を利用してansible-playbook を実行。
3. ansible.logを成果物に保存。

**topics**
1. [git](https://jenkins.io/doc/pipeline/steps/git/)
2. [withDockerContainer](https://jenkins.io/doc/pipeline/steps/docker-workflow/#withdockercontainer-run-build-steps-inside-a-docker-container)
3. [ansiblePlaybook](https://jenkins.io/doc/pipeline/steps/ansible/#ansibleplaybook-invoke-an-ansible-playbook)

**sample code**

```run_ansible.groovy
def ANSIBLE_CONFIG = """\
[defaults]
log_path = ./ansible.log
"""

pipeline {
    agent any

    stages {
        stage('set up ansible files') {
            steps {
                deleteDir()
                git 'https://github.com/chusiang/helloworld.ansible.role'
                writeFile(file: "ansible.cfg", text: "${ANSIBLE_CONFIG}")
            }
        }

        stage('run helloworld.yml') {
            steps {
                withDockerContainer(args: '-u 0', image: 'williamyeh/ansible:alpine3') {
                    echo "show ansible version"
                    sh "ansible --version"

                    echo "run ansible-playbook"
                    ansiblePlaybook(
                        playbook: 'setup.yml',
                        extras: '-c local'
                    )
                }
            }
            post {
                always {
                    archiveArtifacts 'ansible.log'
                }
            }
        }
    }
    post {
        success {
            cleanWs()
        }
    }
}
```
**link**

* [sorce code](https://github.com/sakamaki-y123/jenkins-continuous-delivery/blob/master/pipeline/decrative-pipeline/pipeline_14_run_ansible.groovy)
* [Jenkins sample job](http://118.27.19.114:8080/job/decrative-pipeline/job/14_run_ansible/)

---

# 参考ページ

## jenkins 公式ページ
* [Using a Jenkinsfile ](https://jenkins.io/doc/book/pipeline/jenkinsfile/)
* [Pipeline Syntax](https://jenkins.io/doc/book/pipeline/syntax/#declarative-pipeline)
* [Pipeline Steps Reference](https://jenkins.io/doc/pipeline/steps/)
* [Pipeline Shared Libraries](https://jenkins.io/doc/book/pipeline/shared-libraries/)

## Qiita
* [Jenkins2のPipline参考リンク集](https://qiita.com/Takumon/items/e266146c225d07b82c13#declarative-pipeline)
* [Declarative PipelineでJenkinsfileを書いてみた(Checkstyle,Findbugs,PMD,CPDとか)](https://qiita.com/Jenkins_BBA/items/bc60a8b98b4665a75755#_reference-c4a2802b77f9c0cf5ffd)
* [【Jenkins】Declarative Pipeline入門](https://qiita.com/Toriyabot/items/babc28db2738ed6cbac2)

## その他
* [Jenkins 2.0 (3): Scripted Pipeline と Declarative Pipeline](http://www.kaizenprogrammer.com/entry/2017/02/14/230714)
* [Jenkins2 Pipelines 101](https://www.cakesolutions.net/teamblogs/jenkins2-pipelines-101)
* https://github.com/chusiang/helloworld.ansible.role
* https://hub.docker.com/r/williamyeh/ansible/
* https://automatingguy.com/2017/12/29/jenkins-pipelines-shared-libraries/

