package br.com.magnus.config.starter.file;

import br.com.magnus.config.starter.configuration.JavaParserSingleton;
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
    private final JavaParser javaParser = JavaParserSingleton.getInstance();

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
                clone.setParsed(javaParser.parse(clone.getParsed().toString()).getResult().orElseThrow(() -> new IllegalArgumentException("Error parsing JavaFile")));
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Error cloning JavaFile", e);
        }
    }

	public Object getName() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getName'");
	}
}
