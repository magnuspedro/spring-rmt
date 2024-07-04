package br.com.magnus.config.starter.members.detectors.methods;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Builder
public record Reference(String title, int year, List<String> authors) implements Serializable {

    public String getAuthors() {
        return String.join(", ", authors);
    }

}
