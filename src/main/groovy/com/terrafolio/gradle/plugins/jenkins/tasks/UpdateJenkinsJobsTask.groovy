package com.terrafolio.gradle.plugins.jenkins.tasks

import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsConfigurable
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsServerDefinition
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsView
import com.terrafolio.gradle.plugins.jenkins.service.JenkinsService
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

class UpdateJenkinsJobsTask extends AbstractJenkinsTask {
    def jobsToUpdate = []
    def viewsToUpdate = []

    def void doExecute() {
        (jobsToUpdate + viewsToUpdate).each { JenkinsConfigurable item ->
            eachServer(item) { JenkinsServerDefinition server, JenkinsService service ->
                def existing = service.getConfiguration(item.configurableName, item.serviceOverrides.get)
                if (existing == null) {
                    logger.warn('Creating new item ' + item.configurableName + ' on ' + server.url)
                    service.createConfiguration(item.configurableName, item.getServerSpecificXml(server), item.serviceOverrides.create)
                } else {
                    XMLUnit.setIgnoreWhitespace(true)
                    def Diff xmlDiff = new Diff(item.getServerSpecificXml(server), existing)
                    if ((! xmlDiff.similar()) || (project.hasProperty('forceJenkinsJobsUpdate') && Boolean.valueOf(project.forceJenkinsJobsUpdate))) {
                        logger.warn('Updating item ' + item.configurableName + ' on ' + server.url)
                        service.updateConfiguration(item.configurableName, item.getServerSpecificXml(server), item.serviceOverrides.update)
                    } else {
                        logger.warn('Jenkins item ' + item.configurableName + ' has no changes to the existing item on ' + server.url)
                    }
                }
            }
        }
    }

    def void update(JenkinsConfigurable item) {
        if (item instanceof JenkinsJob) {
            jobsToUpdate += item
        }

        if (item instanceof JenkinsView) {
            viewsToUpdate += item
        }
    }
}
