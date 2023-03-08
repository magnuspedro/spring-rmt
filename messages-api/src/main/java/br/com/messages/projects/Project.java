package br.com.messages.projects;

import br.com.messages.files.FileEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Supplier;

@SuperBuilder
public class Project extends FileEntity {

	private static final long serialVersionUID = 1L;

	@Builder.Default
	private ProjectPhase phase = ProjectPhase.ON_HOLD;

	private final @JsonIgnore Supplier<InputStream> contentHandler;

	private final String originalId;

	public Project() {
		super();
		this.contentHandler = () -> null;
		this.originalId = null;
	}

	public Project(String id, String name) {
		super(id, name);
		this.contentHandler = () -> null;
		this.originalId = null;
	}

	public Project(String id, String name, Supplier<InputStream> contentHandler, String contentType) {
		this(id, name, contentHandler, contentType, null);
	}

	public Project(String id, String name, Supplier<InputStream> contentHandler, String contentType,
			String originalId) {
		super(id, name, contentType);
		this.contentHandler = contentHandler;
		this.originalId = originalId;
	}

	public ProjectPhase getPhase() {
		return phase;
	}

	public void setPhase(ProjectPhase phase) {
		this.phase = phase;
	}

	public boolean isOnHold() {
		return ProjectPhase.ON_HOLD.equals(this.phase);
	}

	public String getOriginalId() {
		return originalId;
	}

	public byte[] getContent() {
		return Optional.ofNullable(this.contentHandler).map(Supplier::get).map(c -> {
			try {
				return IOUtils.toByteArray(c);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).orElse(new byte[0]);
	}

	public InputStream getStream() {
		return Optional.ofNullable(this.contentHandler).map(Supplier::get).orElse(null);
	}

	public void sendContentTo(OutputStream out) throws IOException {
		IOUtils.copy(this.getStream(), out);
	}

}
