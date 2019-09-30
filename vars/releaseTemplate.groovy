#!/usr/bin/groovy
import io.fabric8.Fabric8Commands

def call(Map parameters = [:], body) {
    def flow = new Fabric8Commands()

    def defaultLabel = buildId('release')
    def label = parameters.get('label', defaultLabel)

    def mavenImage = parameters.get('mavenImage', 'fabric8/maven-builder:v7973e33')
    def clientsImage = parameters.get('clientsImage', 'fabric8/builder-clients:v703b6d9')
    def dockerImage = parameters.get('dockerImage', 'docker:1.11')
    def inheritFrom = parameters.get('inheritFrom', 'base')
    def jnlpImage = (flow.isOpenShift()) ? 'fabric8/jenkins-slave-base-centos7:v54e55b7' : 'jenkinsci/jnlp-slave:2.62'
    def cloud = flow.getCloudConfig()

    def utils = new io.fabric8.Utils()

    podTemplate(cloud: cloud, label: label, inheritFrom: "${inheritFrom}",
            containers: [
                    containerTemplate(
                            name: 'maven',
                            image: "${mavenImage}",
                            command: 'cat',
                            ttyEnabled: true,
                            envVars: [
                                    envVar(key: 'MAVEN_OPTS', value: '-Duser.home=/root/')
                            ]
                    ),
                    containerTemplate(
                            name: 'clients',
                            image: "${clientsImage}",
                            command: 'cat',
                            ttyEnabled: true,
                            workingDir: '/home/jenkins/',
                            envVars: [
                                    envVar(key: 'TERM', value: 'dumb')
                            ]),
                    containerTemplate(
                            name: 'docker',
                            image: "${dockerImage}",
                            command: 'cat',
                            ttyEnabled: true,
                            workingDir: '/home/jenkins/',
                            envVars: [
                                    envVar(key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/')
                            ])
            ],
            volumes: [secretVolume(secretName: 'jenkins-maven-settings', mountPath: '/root/.m2'),
                      secretVolume(secretName: 'jenkins-docker-cfg', mountPath: '/home/jenkins/.docker'),
                      secretVolume(secretName: 'jenkins-release-gpg', mountPath: '/home/jenkins/.gnupg-ro'),
                      secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                      secretVolume(secretName: 'jenkins-ssh-config', mountPath: '/root/.ssh-ro'),
                      secretVolume(secretName: 'jenkins-git-ssh', mountPath: '/root/.ssh-git-ro'),
                      secretVolume(secretName: 'gke-service-account', mountPath: '/root/home/.gke'),
                      hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')]
    ) {

        body(

        )
    }
}
