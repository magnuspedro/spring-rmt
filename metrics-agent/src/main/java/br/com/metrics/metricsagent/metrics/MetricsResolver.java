package br.com.metrics.metricsagent.metrics;

public interface MetricsResolver {

    int getDepthOfInheritanceTree();

    int getCyclomaticComplexity();

    int getLinesOfCode();
}
