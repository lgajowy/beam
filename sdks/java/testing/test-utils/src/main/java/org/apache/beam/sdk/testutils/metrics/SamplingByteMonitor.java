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
package org.apache.beam.sdk.testutils.metrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.metrics.Distribution;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor that records the number of bytes flowing through a PCollection.
 *
 * <p>To use: apply a monitor in a desired place in the pipeline. This will capture how many bytes
 * flew through this DoFn. Such information can be then collected and written out and queried using
 * {@link org.apache.beam.sdk.testutils.metrics.MetricsReader}.
 */
public class SamplingByteMonitor<T> extends DoFn<T, T> {

  private static final Logger LOG = LoggerFactory.getLogger(SamplingByteMonitor.class);

  private static final Random SAMPLER = new Random();

  private Distribution totalBytes;

  // This can be passed in constructor form, for example PipelineOptions
  private double samplingRate = 0.0001;

  public SamplingByteMonitor(String namespace, String name) {
    this.totalBytes = Metrics.distribution(namespace, name);
  }

  @ProcessElement
  public void processElement(ProcessContext c) throws CannotProvideCoderException, IOException {
    T element = c.element();

    if (shouldSample()) {
      int size = getBytes(element);
      LOG.info(String.format("Sampling. Sample size: %s, text: %s", size, element.toString()));
      totalBytes.update(size);
    }

    c.output(element);
  }

  private int getBytes(T element) throws CannotProvideCoderException, IOException {
    Coder coder = CoderRegistry.createDefault().getCoder(element.getClass());
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    coder.encode(element, outStream);
    return outStream.size();
  }

  private boolean shouldSample() {
    return samplingRate >= SAMPLER.nextDouble();
  }
}
