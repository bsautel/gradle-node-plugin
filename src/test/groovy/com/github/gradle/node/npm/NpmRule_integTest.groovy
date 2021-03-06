package com.github.gradle.node.npm

import com.github.gradle.AbstractIntegTest
import org.gradle.testkit.runner.TaskOutcome

import java.util.regex.Pattern

class NpmRule_integTest
    extends AbstractIntegTest
{
    def 'execute npm_install rule'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
                workDir = file('build/node')
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = buildTask( 'npm_install' )

        then:
        result.outcome == TaskOutcome.SUCCESS
    }

    def 'Use downloaded npm version'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
            }
        ''' )
        writeEmptyPackageJson()

        when:
        def result = build( 'npm_run_--version' )

        then:
        result.output =~ /\n6\.4\.1\n/
        result.task( ':npm_run_--version' ).outcome == TaskOutcome.SUCCESS
    }

    def 'Use local npm installation'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
            }
        ''' )
        writeEmptyPackageJson()

        when:
        build( 'npm_install_npm@4.0.2' )
        def result = build( 'npm_run_--version' )

        then:
        result.output =~ /\n4\.0\.2\n/
        result.task( ':npm_run_--version' ).outcome == TaskOutcome.SUCCESS
    }

    def 'can execute an npm module using npm_run_'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
            }
        ''' )

        copyResources( 'fixtures/npm-missing/package.json', 'package.json' )

        when:
        def result = buildTask( 'npm_run_echoTest' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'test.txt' )
    }

    def 'succeeds to run npm module using npm_run_ when the package.json file contains local npm'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }

            node {
                download = true
            }
        ''' )

        copyResources( 'fixtures/npm-present/' )

        when:
        def result = build( 'npm_run_npmVersion' )

        then:
        result.task(":npmInstall").outcome == TaskOutcome.SUCCESS
        result.task(":npm_run_npmVersion").outcome == TaskOutcome.SUCCESS
        def versionPattern = Pattern.compile(".*Version\\s+6.12.0.*", Pattern.DOTALL)
        versionPattern.matcher(result.output).find()
    }

    def 'can execute subtasks using npm'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
            }
        ''' )
        writePackageJson(""" {
            "name": "example",
            "dependencies": {},
            "scripts": {
                "parent" : "echo 'parent1' > parent1.txt && npm run child1 && npm run child2 && echo 'parent2' > parent2.txt",
                "child1": "echo 'child1' > child1.txt",
                "child2": "echo 'child2' > child2.txt"
            }
        }
        """)

        when:
        def result = buildTask( 'npm_run_parent' )

        then:
        result.outcome == TaskOutcome.SUCCESS
        fileExists( 'parent1.txt' )
        fileExists( 'child1.txt' )
        fileExists( 'child2.txt' )
        fileExists( 'parent2.txt' )
    }

    def 'Custom workingDir'()
    {
        given:
        writeBuild( '''
            plugins {
                id 'com.github.node-gradle.node'
            }
            node {
                download = true
                nodeModulesDir = file("frontend")
            }
        ''' )
        writeFile( 'frontend/package.json', """{
            "name": "example",
            "dependencies": {},
            "scripts": {
                "whatVersion": "npm run --version"
            }
        }""" )

        when:
        def result = build( 'npm_run_whatVersion' )

        then:
        result.output =~ /\n6\.4\.1\n/
        result.task( ':npm_run_whatVersion' ).outcome == TaskOutcome.SUCCESS
    }
}
