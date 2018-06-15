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

import static org.apache.beam.sdk.values.TypeDescriptors.strings;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.parquet.ParquetIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;

/**
 * An example that writes using parquetIO.
 */
public class ParquetRead {

  private static final Schema SCHEMA = new Schema.Parser().parse("{\n"
    + " \"namespace\": \"ioitParquet\",\n"
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


    pipeline.apply("Find files", FileIO.match().filepattern(options.getFilenamePrefix()))
      .apply("Read matched files", FileIO.readMatches())
      .apply("Read parquet files", ParquetIO.readFiles(SCHEMA))
      .apply("Map records to strings", MapElements.into(strings()).via(new GetRecordsFn()))
      .apply(ParDo.of(new PrintRecords()));

    pipeline.run().waitUntilFinish();
  }


  private static class GetRecordsFn implements SerializableFunction<GenericRecord, String> {

    @Override
    public String apply(GenericRecord input) {
      return String.valueOf(input.get("row"));
    }
  }

  private static class PrintRecords extends DoFn<String, String> {
    @ProcessElement
    public void processElement(ProcessContext c) {
      System.out.println(c.element());
      c.output(c.element());
    }
  }
}
