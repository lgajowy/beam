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
 * <p>This monitor is for any type but uses sampling. This is due to the fact, that getting the size
 * of a generic object is computationally expensive.
 */
public class SamplingByteMonitor<T> extends DoFn<T, T> {

  private static final Logger LOG = LoggerFactory.getLogger(SamplingByteMonitor.class);

  // TODO: will this be the same random for all bundles?
  // NO IT WON'T
  // USE STATE API FOR THE RANDOM AND FOR SAMPLE COUNT
  // Is using state api safe and not computationally expensive?
  // use windowing per each record and sample using sliding windows?

  // https://beam.apache.org/blog/2017/08/28/timely-processing.html

  private static final Random SAMPLER = new Random(1000);

  private Distribution recordSizeSamples;

  private double samplingRate;

  /**
   * Creates the monitor.
   *
   * @param namespace - namespace of size distribution metric
   * @param name - name of size distribution metric
   * @param samplingRate - defines how often should the monitor sample. The bigger the sample, the
   *     more frequent sampling is. Samples are distributed uniformly according to the rate across
   *     all PCollection elements that are passed to this {@link DoFn}.
   */
  public SamplingByteMonitor(String namespace, String name, double samplingRate) {
    this.samplingRate = samplingRate;
    this.recordSizeSamples = Metrics.distribution(namespace, name);
  }

  @ProcessElement
  public void processElement(ProcessContext c) throws CannotProvideCoderException, IOException {
    T element = c.element();

    if (shouldSample()) {
      int size = getBytes(element);
      recordSizeSamples.update(size);
      LOG.info("Sample! | Sample size: {}", size);
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
