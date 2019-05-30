package org.apache.beam.runners.flink;

import org.apache.beam.vendor.grpc.v1p13p1.com.google.common.collect.Lists;
import java.util.Collection;
import org.apache.beam.model.pipeline.v1.MetricsApi;
import org.apache.beam.runners.core.metrics.MetricsContainerStepMap;
import org.apache.beam.runners.fnexecution.jobsubmission.PortablePipelineResult;
import org.apache.beam.sdk.PipelineResult;

public class FlinkPortablePipelineResult extends PortablePipelineResult {

  public FlinkPortablePipelineResult(PipelineResult result) {
    super(result);
  }

  @Override
  public Collection<MetricsApi.MonitoringInfo> portableMetrics() {
    FlinkRunnerResult flinkResult = (FlinkRunnerResult) this.result;

    MetricsContainerStepMap metricsContainerStepMap = flinkResult.getMetricsContainerStepMap();

    return Lists.newArrayList(metricsContainerStepMap.getMonitoringInfos());
  }
}
