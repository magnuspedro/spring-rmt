package br.com.detection.detectionagent.methods.dataExtractions;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.exception.NullJavaFileException;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AbstractSyntaxTree implements ExtractionMethod {
    @Override
    public List<Object> parseAll(List<JavaFile> files) {
        return Optional.ofNullable(files)
                .orElseThrow(NullJavaFileException::new)
                .stream()
                .map(this::parseSingle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Object parseSingle(JavaFile file) {
        if (file == null) {
            throw new NullJavaFileException();
        }
        CompilationUnit parsed = null;

        try {
            parsed = JavaParser.parse(file.getInputStream());
            ;
            file.setParsed(parsed);
        } catch (Exception e) {
            log.error("Error parsing file: {}", file.getPath(), e);
        }
        return parsed;
    }

    @Override
    public Boolean supports(Object object) {
        return object instanceof AbstractSyntaxTreeDependent;
    }

}
