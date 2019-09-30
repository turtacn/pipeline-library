#!/usr/bin/groovy
import io.fabric8.Fabric8Commands

def call(Map parameters = [:], body) {
    def flow = new Fabric8Commands()
    def defaultLabel = buildId('go')
    def label = parameters.get('label', defaultLabel)

    def goImage = parameters.get('goImage', 'fabric8/go-builder:1.8.1.2')
    def clientsImage = parameters.get('clientsImage', 'fabric8/builder-clients:v703b6d9')
    def inheritFrom = parameters.get('inheritFrom', 'base')
    def jnlpImage = (flow.isOpenShift()) ? 'fabric8/jenkins-slave-base-centos7:v54e55b7' : 'jenkinsci/jnlp-slave:2.62'

    def utils = new io.fabric8.Utils()
    podTemplate(label: label, serviceAccount: 'jenkins', inheritFrom: "${inheritFrom}",
            containers: [
                    containerTemplate(
                            name: 'go',
                            image: "${goImage}",
                            command: '/bin/sh -c',
                            args: 'cat',
                            ttyEnabled: true,
                            workingDir: '/home/jenkins/',
                            envVars: [
                                    envVar(key: 'GOPATH', value: '/home/jenkins/go')
                            ]),
                    containerTemplate(
                            name: 'clients',
                            image: "${clientsImage}",
                            command: 'cat',
                            ttyEnabled: true)
            ],
            volumes:
                    [secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                     secretVolume(secretName: 'jenkins-ssh-config', mountPath: '/root/.ssh-ro'),
                     secretVolume(secretName: 'jenkins-git-ssh', mountPath: '/root/.ssh-git-ro')
                    ]) {
        body()

    }
}
