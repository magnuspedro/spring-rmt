package br.com.magnus.config.starter.configuration;

import com.github.javaparser.JavaParser;

public class JavaParserSingleton {
    private static JavaParser instance;

    private JavaParserSingleton() {
    }

    public static JavaParser getInstance() {
        if (instance == null) {
            instance = new JavaParser();
        }
        return instance;
    }
}
