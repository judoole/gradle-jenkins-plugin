package com.terrafolio.gradle.plugins.jenkins.tasks

class DeleteAllJenkinsJobsTask extends DeleteJenkinsJobsTask {

    @Override
    def void doExecute() {
        jobsToDelete = getJobs()
        viewsToDelete = getViews()
        super.doExecute()
    }

}
