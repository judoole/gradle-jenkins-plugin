package com.terrafolio.gradle.plugins.jenkins.test.dsl

import com.terrafolio.gradle.plugins.jenkins.JenkinsPlugin
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsJob
import com.terrafolio.gradle.plugins.jenkins.dsl.JenkinsView
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.dsl.ViewType
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

/**
 * Created by ghale on 4/7/14.
 */
class JenkinsConfigurationTest {
    def private final Project project = ProjectBuilder.builder().withProjectDir(new File('build/tmp/test')).build()
    def private final JenkinsPlugin plugin = new JenkinsPlugin()

    @Before
    def void setupProject() {
        plugin.apply(project)
    }

    @Test
    void configure_addsJenkinsJob() {
        project.jenkins {
            jobs {
                testJob
            }
        }

        assert project.convention.plugins.jenkins.jenkins.jobs.findByName('testJob') instanceof JenkinsJob
    }

    @Test
    void configure_addsJenkinsView() {
        project.jenkins {
            views {
                test
            }
        }

        assert project.convention.plugins.jenkins.jenkins.views.findByName('test') instanceof JenkinsView
    }

    @Test
    def void configure_configuresJobsUsingDslFile() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..9) {
                job {
                    name "Test Job \${i}"
                }
            }
        """)
        project.jenkins {
            dsl project.files('test.dsl')
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <description></description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers class='vector'></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.jobs.size() == 10
        def jobNames = (0..9).collect { "Test Job ${it}" }
        project.jenkins.jobs.each { job ->
            assert jobNames.find { it == job.name } != null
            jobNames.remove(job.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
            assert xmlDiff.similar()
        }

    }

    @Test
    def void configure_configuresJobsUsingMultipleDslFiles() {
        def jenkinsDir = project.file('jenkins')
        jenkinsDir.mkdirs()
        def dslFile1 = new File(jenkinsDir, 'test1.dsl')
        dslFile1.write("""
            for (i in 0..9) {
                job {
                    name "Test Job \${i}"
                }
            }
        """)

        def dslFile2 = new File(jenkinsDir, 'test2.dsl')
        dslFile2.write("""
            for (i in 0..9) {
                job {
                    name "Another Job \${i}"
                }
            }
        """)

        project.jenkins {
            dsl project.fileTree("jenkins").include("*.dsl")
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <description></description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers class='vector'></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.jobs.size() == 20
        def jobNames = (0..9).collect { "Test Job ${it}" } + (0..9).collect { "Another Job ${it}" }
        project.jenkins.jobs.each { job ->
            assert jobNames.find { it == job.name } != null
            jobNames.remove(job.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
            assert xmlDiff.similar()
        }
    }

    @Test
    def void configure_configuresViewsUsingDslFile() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..9) {
                view(type: ListView) {
                    name("test view \${i}")
                }
            }
        """)
        project.jenkins {
            dsl project.files('test.dsl')
        }

        def expectedXml = """
            <hudson.model.ListView>
              <filterExecutors>false</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.views.size() == 10
        def viewNames = (0..9).collect { "test view ${it}" }
        project.jenkins.views.each { view ->
            assert viewNames.find { it == view.name } != null
            viewNames.remove(view.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, view.xml))
            assert xmlDiff.similar()
        }
    }

    @Test
    def void configure_configuresViewsUsingMultipleDslFiles() {
        def jenkinsDir = project.file('jenkins')
        jenkinsDir.mkdirs()
        def dslFile1 = new File(jenkinsDir, 'test1.dsl')
        dslFile1.write("""
            for (i in 0..9) {
                view(type: ListView) {
                    name "test view \${i}"
                }
            }
        """)

        def dslFile2 = new File(jenkinsDir, 'test2.dsl')
        dslFile2.write("""
            for (i in 0..9) {
                view(type: ListView) {
                    name "another view \${i}"
                }
            }
        """)

        project.jenkins {
            dsl project.fileTree("jenkins").include("*.dsl")
        }

        def expectedXml = """
            <hudson.model.ListView>
              <filterExecutors>false</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.views.size() == 20
        def viewNames = (0..9).collect { "test view ${it}" } + (0..9).collect { "another view ${it}" }
        project.jenkins.jobs.each { view ->
            assert viewNames.find { it == view.name } != null
            viewNames.remove(view.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, view.xml))
            assert xmlDiff.similar()
        }
    }

    @Test
    def void configure_configuresJobsUsingDslClosure() {
        project.jenkins {
            dsl {
                for (i in 0..9) {
                    job {
                        name "Test Job ${i}"
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <description></description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers class='vector'></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.jobs.size() == 10
        def jobNames = (0..9).collect { "Test Job ${it}" }
        project.jenkins.jobs.each { job ->
            assert jobNames.find { it == job.name } != null
            jobNames.remove(job.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
            assert xmlDiff.similar()
        }

    }

    @Test
    def void configure_configuresViewsUsingDslClosure() {
        project.jenkins {
            dsl {
                for (i in 0..9) {
                    view(type: ViewType.ListView) {
                        name "test view ${i}"
                    }
                }
            }
        }

        def expectedXml = """
            <hudson.model.ListView>
              <filterExecutors>false</filterExecutors>
              <filterQueue>false</filterQueue>
              <properties class="hudson.model.View\$PropertyList"/>
              <jobNames class="tree-set">
                <comparator class="hudson.util.CaseInsensitiveComparator"/>
              </jobNames>
              <jobFilters/>
              <columns/>
            </hudson.model.ListView>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.views.size() == 10
        def viewNames = (0..9).collect { "test view ${it}" }
        project.jenkins.views.each { view ->
            assert viewNames.find { it == view.name } != null
            viewNames.remove(view.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, view.xml))
            assert xmlDiff.similar()
        }

    }

    @Test(expected = NameNotProvidedException)
    def void configure_dslClosureThrowsExceptionWhenNoJobNameProvided() {
        project.jenkins {
            dsl {
                for (i in 0..9) {
                    job {
                        steps {
                            shell("echo test")
                        }
                    }
                }
            }
        }
    }

    @Test(expected = NameNotProvidedException)
    def void configure_dslFileThrowsExceptionWhenNoJobNameProvided() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..9) {
                job {
                    steps {
                        shell "echo test"
                    }
                }
            }
        """)
        project.jenkins {
            dsl project.files('test.dsl')
        }
    }

    @Test
    def void configure_configuresJobsUsingDslClosureWithBasisXml() {
        project.jenkins {
            jobs {
                for (i in 0..9) {
                    "Test Job ${i}" {
                        definition {
                            xml """
                                <project>
                                    <actions></actions>
                                    <description>A Description</description>
                                    <keepDependencies>false</keepDependencies>
                                    <properties></properties>
                                    <scm class='hudson.scm.NullSCM'></scm>
                                    <canRoam>true</canRoam>
                                    <disabled>false</disabled>
                                    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                                    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                                    <triggers class='vector'></triggers>
                                    <concurrentBuild>false</concurrentBuild>
                                    <builders></builders>
                                    <publishers></publishers>
                                    <buildWrappers></buildWrappers>
                                </project>
                            """
                        }
                    }
                }
            }
            dsl {
                for (i in 0..9) {
                    job {
                        using "Test Job ${i}"
                        name "Test Job ${i}"
                        displayName "Some Display Name"
                    }
                }
            }
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>A Description</description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers class='vector'></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.jobs.size() == 10
        def jobNames = (0..9).collect { "Test Job ${it}" }
        project.jenkins.jobs.each { job ->
            assert jobNames.find { it == job.name } != null
            jobNames.remove(job.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
            assert xmlDiff.similar()
        }

    }

    @Test
    def void configure_configuresJobsUsingDslFileWithBasisXml() {
        def dslFile = project.file('test.dsl')
        dslFile.write("""
            for (i in 0..9) {
                job {
                    name "Test Job \${i}"
                    using "Test Job \${i}"
                    displayName "Some Display Name"
                }
            }
        """)

        project.jenkins {
            jobs {
                for (i in 0..9) {
                    "Test Job ${i}" {
                        definition {
                            xml """
                                <project>
                                    <actions></actions>
                                    <description>A Description</description>
                                    <keepDependencies>false</keepDependencies>
                                    <properties></properties>
                                    <scm class='hudson.scm.NullSCM'></scm>
                                    <canRoam>true</canRoam>
                                    <disabled>false</disabled>
                                    <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                                    <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                                    <triggers class='vector'></triggers>
                                    <concurrentBuild>false</concurrentBuild>
                                    <builders></builders>
                                    <publishers></publishers>
                                    <buildWrappers></buildWrappers>
                                </project>
                            """
                        }
                    }
                }
            }
            dsl project.files('test.dsl')
        }

        def expectedXml = """
            <project>
                <actions></actions>
                <displayName>Some Display Name</displayName>
                <description>A Description</description>
                <keepDependencies>false</keepDependencies>
                <properties></properties>
                <scm class='hudson.scm.NullSCM'></scm>
                <canRoam>true</canRoam>
                <disabled>false</disabled>
                <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
                <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
                <triggers class='vector'></triggers>
                <concurrentBuild>false</concurrentBuild>
                <builders></builders>
                <publishers></publishers>
                <buildWrappers></buildWrappers>
            </project>
        """

        XMLUnit.setIgnoreWhitespace(true)
        assert project.jenkins.jobs.size() == 10
        def jobNames = (0..9).collect { "Test Job ${it}" }
        project.jenkins.jobs.each { job ->
            assert jobNames.find { it == job.name } != null
            jobNames.remove(job.name)
            def xmlDiff = new DetailedDiff(new Diff(expectedXml, job.definition.xml))
            assert xmlDiff.similar()
        }

    }
}
