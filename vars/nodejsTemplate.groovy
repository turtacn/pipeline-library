#!/usr/bin/groovy
import io.fabric8.Fabric8Commands

def call(Map parameters = [:], body) {
    def flow = new Fabric8Commands()

    def defaultLabel = buildId('nodejs')
    def label = parameters.get('label', defaultLabel)

    def nodejsImage = parameters.get('nodejsImage', 'fabric8/nodejs-builder:0.0.3')
    def inheritFrom = parameters.get('inheritFrom', 'base')
    def jnlpImage = (flow.isOpenShift()) ? 'fabric8/jenkins-slave-base-centos7:v54e55b7' : 'jenkinsci/jnlp-slave:2.62'

    def cloud = flow.getCloudConfig()

    def utils = new io.fabric8.Utils()

    podTemplate(cloud: cloud, label: label, inheritFrom: "${inheritFrom}",
            containers: [
                    containerTemplate(
                            name: 'nodejs',
                            image: "${nodejsImage}",
                            command: '/bin/sh -c',
                            args: 'cat',
                            ttyEnabled: true,
                            workingDir: '/home/jenkins/')
            ]
    ) {
        body()
    }
}
