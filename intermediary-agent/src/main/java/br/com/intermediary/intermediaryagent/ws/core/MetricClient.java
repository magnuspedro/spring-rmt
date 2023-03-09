package br.com.intermediary.intermediaryagent.ws.core;

import br.com.messages.members.api.metrics.MetricsAgentApi;
import br.com.messages.members.metrics.QualityAttributeResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.List;

@FeignClient(name = "metric-client", url="${services.metrics}")
public interface MetricClient {

    @GetMapping(path = MetricsAgentApi.METRICS_PATH + MetricsAgentApi.ROOT + "{projectId}/{refactoredId}")
    List<QualityAttributeResultDTO> evaluate(URI url, @PathVariable("projectId") String projectId, @PathVariable("refactoredId") String refactoredId);
}
