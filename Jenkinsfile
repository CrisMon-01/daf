pipeline{
    agent any
    stages {
         stage('Build') {
         steps {
            script{
                if(env.BRANCH_NAME=='testci' ){
                    sh '''
                    cd security_manager;
                    STAGING=true;
                    sbt " -DSTAGING=$STAGING; reload; clean; compile;  docker:publish";
                    '''
                    sh '''
                    cd storage_manager;
                    STAGING=true;
                    sbt " -DSTAGING=$STAGING; reload; clean; compile;  docker:publish";
                    '''
                    sh '''
                    cd ;
                    STAGING=true;
                    sbt " -DSTAGING=$STAGING; reload; clean; compile;  docker:publish";
                    '''
                    }
                }
            }
        }
        stage('Staging'){
            steps{
            script{
                if(env.BRANCH_NAME=='testci'){
                    sh '''
                        cd security_manager/kubernetes;
                        pwd;
                        ls
                    '''
                    }
                }
            }
        }
    }
}