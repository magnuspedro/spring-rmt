package br.com.detection.detectionagent.methods.dataExtractions;

import com.github.javaparser.JavaParser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AbstractSyntaxTree implements DataExtractionApproach {

    @Override
    public Collection<Object> parseAll(Path... files) {
        return Stream.of(files).map(this::parseSingle).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Object> parseSingle(Path file) {
        try (final FileInputStream fis = new FileInputStream(file.toFile())) {
            return Optional.of(JavaParser.parse(fis));
        } catch (Exception e) {
            log.error("---------------------------------------------------------------");
            log.error("Failed to parse file {}", Optional.ofNullable(file).map(Path::toFile).map(File::getName).orElse(null));
            log.error("Exception:", e);
            log.error("---------------------------------------------------------------");
            return Optional.empty();
        }
    }

}
