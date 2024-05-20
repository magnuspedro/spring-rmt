package br.com.magnus.config.starter.file;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
public class JavaFile implements Cloneable {
    private final String name;
    private final String path;
    private final String originalClass;
    @Setter
    private Object parsed;

    public CompilationUnit getCompilationUnit() {
        if (this.parsed instanceof CompilationUnit)
            return (CompilationUnit) this.parsed;
        return null;
    }

    public String getFileNameWithoutExtension() {
        return this.name.substring(0, this.name.lastIndexOf('.'));
    }

    public String getFullName() {
        return this.path + this.name;
    }

    @Override
    public JavaFile clone() {
        try {
            var clone = (JavaFile) super.clone();
            if (clone.getParsed() instanceof CompilationUnit) {
                clone.setParsed(JavaParser.parse(clone.getParsed().toString()));
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Error cloning JavaFile", e);
        }
    }
}
