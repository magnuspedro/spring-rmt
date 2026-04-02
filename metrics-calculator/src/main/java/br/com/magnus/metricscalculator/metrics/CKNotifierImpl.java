package br.com.magnus.metricscalculator.metrics;

import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKNotifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

@Slf4j
@Getter
public class CKNotifierImpl implements CKNotifier, MetricsResolver {

    private final HashMap<String, CKClassResult> results = new HashMap<>();

    @Override
    public void notify(CKClassResult result) {
        results.put(result.getClassName(), result);
    }

    @Override
    public void notifyError(String sourceFilePath, Exception e) {
        log.error("Error in {}", sourceFilePath, e);
    }

    @Override
    public int getDepthOfInheritanceTree() {
        return (int) Math.round(results.values().stream().mapToInt(CKClassResult::getDit).average().orElse(0));
    }

    @Override
    public int getCyclomaticComplexity() {
        return (int) Math.round(results.values().stream().mapToInt(CKClassResult::getWmc).average().orElse(0));
    }

    @Override
    public int getLinesOfCode() {
        return (int) Math.round(results.values().stream().mapToInt(CKClassResult::getLoc).average().orElse(0));
    }
}