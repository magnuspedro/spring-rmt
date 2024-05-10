package br.com.magnus.config.starter.file;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class JavaFile {
    private final String name;
    private final String path;
    private final String originalClass;
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
}
