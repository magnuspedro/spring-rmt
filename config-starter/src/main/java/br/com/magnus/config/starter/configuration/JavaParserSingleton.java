package br.com.magnus.config.starter.configuration;

import com.github.javaparser.JavaParser;

import java.util.function.Supplier;

public class JavaParserSingleton {
    private static final ScopedValue<JavaParser> instance = ScopedValue.newInstance();

    private JavaParserSingleton() {
    }

    public static JavaParser getInstance() {
        return instance.isBound() ? instance.get() : new JavaParser();
    }

    public static <T> T callWithScopedParser(Supplier<T> supplier) {
        return ScopedValue.where(instance, new JavaParser()).call(supplier::get);
    }
}
