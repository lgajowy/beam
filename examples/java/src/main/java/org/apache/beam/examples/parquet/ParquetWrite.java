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
package org.apache.beam.examples.parquet;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.io.parquet.ParquetIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;

/**
 * An example that writes using parquetIO.
 */
public class ParquetWrite {

  private static final Schema SCHEMA = new Schema.Parser().parse("{\n"
    + " \"namespace\": \"ioitparquet\",\n"
    + " \"type\": \"record\",\n"
    + " \"name\": \"TestParquetLine\",\n"
    + " \"fields\": [\n"
    + "     {\"name\": \"row\", \"type\": \"string\"}\n"
    + " ]\n"
    + "}");

  public static void main(String[] args) {
    PipelineOptionsFactory.register(ParquetExamplePipelineOptions.class);
    ParquetExamplePipelineOptions options = PipelineOptionsFactory
      .fromArgs(args)
      .as(ParquetExamplePipelineOptions.class);
    Pipeline pipeline = Pipeline.create(options);

    pipeline.apply("Generate sequence", GenerateSequence.from(0).to(options.getNumberOfRecords()))
      .apply("Produce text lines", ParDo.of(new DeterministicallyConstructTestTextLineFn()))
      .apply("Produce Avro records", ParDo.of(new DeterministicallyConstructAvroRecordsFn()))
      .setCoder(AvroCoder.of(SCHEMA))
      .apply("Write Parquet files", FileIO.<GenericRecord>write()
        .via(ParquetIO.sink(SCHEMA))
        .to(options.getFilenamePrefix()));

    pipeline.run().waitUntilFinish();
  }

  private static class DeterministicallyConstructTestTextLineFn extends DoFn<Long, String> {
    @ProcessElement public void processElement(ProcessContext c) {
      c.output(String.format("IO IT Test line of text. Line seed: %s", c.element()));
    }
  }

  private static class DeterministicallyConstructAvroRecordsFn extends DoFn<String, GenericRecord> {

    @ProcessElement public void processElement(ProcessContext c) {
      c.output(new GenericRecordBuilder(SCHEMA).set("row", c.element()).build());
    }
  }

}
