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

import CommonJobProperties as common

interface Step {
  create(context)
}

class StepList implements Step {
  List<Step> steps

  @Override
  def create(context) {
    steps.forEach { it.create(context)}
  }
}

abstract class Job { // TODO:do I need that or PhraseTriggeringJob can be reused?
  String name
  String description
  String prTriggerPhrase
  String githubName
  StepList steps

  abstract void define(jenkinsJob)
}

class IOITJob extends Job {
  @Override
  void define(jenkinsJob) { //freestyle job
    jenkinsJob(this.name) {
      description(this.description)
      common.setTopLevelMainJobProperties(jenkinsJob)
      common.enablePhraseTriggeringFromPullRequest(jenkinsJob, this.githubName, this.prTriggerPhrase)
      common.setAutoJob(jenkinsJob, 'H */6 * * *')
      this.steps.create(jenkinsJob)
    }
  }
}

class JavaIOTest implements Step { // TODO: what if I want to run Python IOIT in the future?
  String name
  String task
  Map options
  String filesystem
  String runner

  @Override
  def create(context) {
    context {
      steps {
        gradle {
          rootBuildScriptDir(common.checkoutDir)
          common.setGradleSwitches(delegate)
          switches("--info")
          switches("-DintegrationTestPipelineOptions=\'${common.joinPipelineOptions(options)}\'")
          switches("-Dfilesystem=\'${filesystem}\'")
          switches("-DintegrationTestRunner=\'${runner}\'")
          tasks("${task} --tests ${name}")
        }
      }
    }
  }
}

def dataflowSpecificOptions = { name -> [
          runner        : 'DataflowRunner',
          project       : 'apache-beam-testing',
          tempRoot      : 'gs://temp-storage-for-perf-tests',
          filenamePrefix: "gs://temp-storage-for-perf-tests/${name}/\${BUILD_ID}/",
  ]
}

List jobs = [
        new IOITJob(
                name: 'beam_PerformanceTests_TextIOIT',
                description: 'Runs PerfKit tests for TextIOIT',
                prTriggerPhrase: 'Run Java TextIO Performance Test',
                githubName: 'Java TextIO Performance Test',
                steps: new StepList(steps: [
                        new JavaIOTest(
                                name: 'org.apache.beam.sdk.io.text.TextIOIT',
                                options: [
                                        bigQueryDataset: 'beam_performance',
                                        bigQueryTable  : 'textioit_results',
                                        numberOfRecords: '1000000'
                                ] + dataflowSpecificOptions('textIOIT'),
                                filesystem: 'gcs',
                                runner: 'dataflow'
                        )]
                )
        )
]


def jobConfigurations = [
        [
                jobName           : 'beam_PerformanceTests_TextIOIT',
                jobDescription    : 'Runs PerfKit tests for TextIOIT',
                testName           : 'org.apache.beam.sdk.io.text.TextIOIT',
                githubName: 'Java TextIO Performance Test',
                prTriggerPhrase    : 'Run Java TextIO Performance Test',
                testOptions: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'textioit_results',
                        numberOfRecords: '1000000'
                ]

        ],
        [
                jobName            : 'beam_PerformanceTests_Compressed_TextIOIT',
                jobDescription     : 'Runs PerfKit tests for TextIOIT with GZIP compression',
                testName            : 'org.apache.beam.sdk.io.text.TextIOIT',
                githubName : 'Java CompressedTextIO Performance Test',
                prTriggerPhrase     : 'Run Java CompressedTextIO Performance Test',
                testOptions: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'compressed_textioit_results',
                        numberOfRecords: '1000000',
                        compressionType: 'GZIP'
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_ManyFiles_TextIOIT',
                jobDescription    : 'Runs PerfKit tests for TextIOIT with many output files',
                testName           : 'org.apache.beam.sdk.io.text.TextIOIT',
                githubName: 'Java ManyFilesTextIO Performance Test',
                prTriggerPhrase    : 'Run Java ManyFilesTextIO Performance Test',
                testOptions: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'many_files_textioit_results',
                        reportGcsPerformanceMetrics: 'true',
                        gcsPerformanceMetrics: 'true',
                        numberOfRecords: '1000000',
                        numberOfShards: '1000'
                ]

        ],
        [
                jobName           : 'beam_PerformanceTests_AvroIOIT',
                jobDescription    : 'Runs PerfKit tests for AvroIOIT',
                testName           : 'org.apache.beam.sdk.io.avro.AvroIOIT',
                githubName: 'Java AvroIO Performance Test',
                prTriggerPhrase    : 'Run Java AvroIO Performance Test',
                testOptions: [
                        numberOfRecords: '1000000',
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'avroioit_results',
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_TFRecordIOIT',
                jobDescription    : 'Runs PerfKit tests for beam_PerformanceTests_TFRecordIOIT',
                testName           : 'org.apache.beam.sdk.io.tfrecord.TFRecordIOIT',
                githubName: 'Java TFRecordIO Performance Test',
                prTriggerPhrase    : 'Run Java TFRecordIO Performance Test',
                testOptions: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'tfrecordioit_results',
                        numberOfRecords: '1000000'
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_XmlIOIT',
                jobDescription    : 'Runs PerfKit tests for beam_PerformanceTests_XmlIOIT',
                testName           : 'org.apache.beam.sdk.io.xml.XmlIOIT',
                githubName: 'Java XmlIOPerformance Test',
                prTriggerPhrase    : 'Run Java XmlIO Performance Test',
                testOptions: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'xmlioit_results',
                        numberOfRecords: '100000000',
                        charset: 'UTF-8'
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_ParquetIOIT',
                jobDescription    : 'Runs PerfKit tests for beam_PerformanceTests_ParquetIOIT',
                testName           : 'org.apache.beam.sdk.io.parquet.ParquetIOIT',
                githubName: 'Java ParquetIOPerformance Test',
                prTriggerPhrase    : 'Run Java ParquetIO Performance Test',
                testOptions: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'parquetioit_results',
                        numberOfRecords: '100000000'
                ]
        ]
]



for (jobDeclaration in jobs) {
  jobDeclaration.define(job)
}
