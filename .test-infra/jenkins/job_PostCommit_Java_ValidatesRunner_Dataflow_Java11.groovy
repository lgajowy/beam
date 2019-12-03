/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import CommonJobProperties as commonJobProperties
import PostcommitJobBuilder

final String JAVA_11_HOME = '/usr/lib/jvm/java-11-openjdk-amd64'
final String JAVA_8_HOME = '/usr/lib/jvm/java-8-openjdk-amd64'

PostcommitJobBuilder.postCommitJob('beam_PostCommit_Java11_ValidatesRunner_Dataflow',
    'Run Dataflow ValidatesRunner Java 11', 'Google Cloud Dataflow Runner ValidatesRunner Tests On Java 11', this) {

  description('Runs the ValidatesRunner suite on the Dataflow runner with Java 11 worker harness.')

  commonJobProperties.setTopLevelMainJobProperties(delegate, 'master', 270)

  publishers {
    archiveJunit('**/build/test-results/**/*.xml')
  }

  steps {
    gradle {
      rootBuildScriptDir(commonJobProperties.checkoutDir)
      commonJobProperties.setGradleSwitches(delegate)
      tasks(':runners:google-cloud-dataflow-java:compileJava')
      tasks(':runners:google-cloud-dataflow-java:compileTestJava')
      switches("-Dorg.gradle.java.home=${JAVA_8_HOME}")
    }

    gradle {
      rootBuildScriptDir(commonJobProperties.checkoutDir)
      tasks(':runners:google-cloud-dataflow-java:validatesRunner')
      switches("-Djava.specification.version=11")
      switches("-Dorg.gradle.java.home=${JAVA_11_HOME}")
      switches('-x compileJava')
      switches('-x compileTestJava')
      // Increase parallel worker threads above processor limit since most time is
      // spent waiting on Dataflow jobs. ValidatesRunner tests on Dataflow are slow
      // because each one launches a Dataflow job with about 3 mins of overhead.
      // 3 x num_cores strikes a good balance between maxing out parallelism without
      // overloading the machines.
      commonJobProperties.setGradleSwitches(delegate, 3 * Runtime.runtime.availableProcessors())
    }
  }
}
