package br.com.detection.detectionagent.methods.dataExtractions;

import br.com.detection.detectionagent.file.JavaFile;
import br.com.detection.detectionagent.methods.dataExtractions.exception.NullJavaFileException;
import br.com.detection.detectionagent.methods.dataExtractions.forks.AbstractSyntaxTreeDependent;
import com.github.javaparser.JavaParser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class AbstractSyntaxTree implements ExtractionMethod {

    @Override
    public List<Object> parseAll(List<JavaFile> files) {
        return Optional.ofNullable(files)
                .orElseThrow(NullJavaFileException::new)
                .stream()
                .map(this::parseSingle)
                .collect(Collectors.toList());
    }

    @Override
    public Object parseSingle(JavaFile file) {
        if (file == null) {
            throw new NullJavaFileException();
        }

        var parsed = JavaParser.parse(file.getInputStream());
        file.setParsed(parsed);
        return parsed;
    }

    @Override
    public Boolean supports(Object object) {
        return object instanceof AbstractSyntaxTreeDependent;
    }

}
