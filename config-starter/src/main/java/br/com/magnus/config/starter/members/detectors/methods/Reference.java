package br.com.magnus.config.starter.members.detectors.methods;

import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.List;

@Builder
public record Reference(String title, int year, List<String> authors) implements Serializable {

    @Override
    public boolean equals(Object object) {
        if (object instanceof Reference another) {
            if (this.authors().size() != another.authors().size()) {
                return false;
            }

            EqualsBuilder eqBuilder = new EqualsBuilder().append(title, another.title).append(year, another.year);

            for (int i = 0; i < this.authors().size(); i++) {
                eqBuilder = eqBuilder.append(authors.get(i), another.authors.get(i));
            }

            return eqBuilder.isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(title).toHashCode();
    }

    public String getAuthors() {
        return String.join(", ", authors);
    }

}
