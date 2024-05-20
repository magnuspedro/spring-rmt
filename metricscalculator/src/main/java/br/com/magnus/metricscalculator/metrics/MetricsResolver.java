package br.com.magnus.metricscalculator.metrics;

public interface MetricsResolver {

    int getDepthOfInheritanceTree();

    int getCyclomaticComplexity();

    int getLinesOfCode();
}
