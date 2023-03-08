package br.com.messages.files;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@SuperBuilder
@Getter
public class FileEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;

	private final String name;

	private final String contentType;

	public FileEntity() {
		this(null, null, null);
	}

	public FileEntity(String id, String name) {
		this(id, name, null);
	}

	public FileEntity(String id, String name, String contentType) {
		this.id = id;
		this.name = name;
		this.contentType = contentType;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof FileEntity) {
			FileEntity another = (FileEntity) object;
			return new EqualsBuilder().append(id, another.id).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

}
