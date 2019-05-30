package org.apache.beam.runners.fnexecution.jobsubmission;

import java.io.IOException;
import java.util.Collection;
import org.apache.beam.model.pipeline.v1.MetricsApi;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.metrics.MetricResults;
import org.joda.time.Duration;

public class PortablePipelineResult implements PipelineResult {

  protected PipelineResult result;

  public PortablePipelineResult(PipelineResult result) {
    this.result = result;
  }

  @Override
  public State getState() {
    return result.getState();
  }

  @Override
  public State cancel() throws IOException {
    return result.cancel();
  }

  @Override
  public State waitUntilFinish(Duration duration) {
    return result.waitUntilFinish(duration);
  }

  @Override
  public State waitUntilFinish() {
    return result.waitUntilFinish();
  }

  @Override
  public MetricResults metrics() {
    return result.metrics();
  }

  // TODO: should this be runner dependent?
  public Collection<MetricsApi.MonitoringInfo> portableMetrics() {
    throw new UnsupportedOperationException("Not implemented for this runner");
  }
}
