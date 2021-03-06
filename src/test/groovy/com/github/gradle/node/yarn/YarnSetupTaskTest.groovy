package com.github.gradle.node.yarn

import com.github.gradle.node.task.AbstractTaskTest
import com.github.gradle.node.yarn.YarnSetupTask
import org.gradle.process.ExecSpec

class YarnSetupTaskTest
    extends AbstractTaskTest
{
    def "exec yarnSetup task"()
    {
        given:
        this.execSpec = Mock( ExecSpec )

        def task = this.project.tasks.create( 'simple', YarnSetupTask )

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.result.exitValue == 0
        1 * this.execSpec.setArgs( [] )
    }

    def "exec yarnSetup task (version specified)"()
    {
        given:
        this.ext.npmVersion = '6.4.1'
        this.execSpec = Mock( ExecSpec )

        def task = this.project.tasks.create( 'simple', YarnSetupTask )

        when:
        this.project.evaluate()
        task.exec()

        then:
        task.result.exitValue == 0
        1 * this.execSpec.setArgs( [] )
    }
}
