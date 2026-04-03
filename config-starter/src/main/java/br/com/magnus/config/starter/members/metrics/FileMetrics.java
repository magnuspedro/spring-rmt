package br.com.magnus.config.starter.members.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetrics {
    @Builder.Default
    private List<BasicQualityAttributeResult> metrics = new ArrayList<>();
}
