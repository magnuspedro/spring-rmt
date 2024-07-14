package br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
import br.com.magnus.config.starter.file.JavaFile;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ExtractionMethod;
import br.com.magnus.detectionandrefactoring.refactor.dataExtractions.ast.exceptions.NullJavaFileException;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AbstractSyntaxTree implements ExtractionMethod {
    private static final JavaParser javaParser = JavaParserSingleton.getInstance();

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
            parsed = parseFile(file.getOriginalClass());
            file.setParsed(parsed);
        } catch (Exception e) {
            log.error("Error parsing file: {}", file.getPath(), e);
        }
        return parsed;
    }

    public static Object parseSingle(String file) {
        Assert.notNull(file, "File cannot be null");
        CompilationUnit parsed = null;

        try {
            parsed = parseFile(file);
        } catch (Exception e) {
            log.error("Error parsing file: {}", file, e);
        }
        return parsed;
    }

    @Override
    public Boolean supports(Object object) {
        return object instanceof AbstractSyntaxTreeExtraction;
    }

    private static CompilationUnit parseFile(Object file) {
        var parsed = javaParser.parse(file.toString());
        if (parsed.getResult().isEmpty()) {
            log.error("Error parsing Java File: {}", parsed.getProblems());
            throw new IllegalArgumentException("Error parsing Java File ");
        }
        return parsed.getResult().get();
    }

}
