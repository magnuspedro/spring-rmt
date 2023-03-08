package br.com.messages.files;

public enum FileRepositoryCollections {
	PROJECTS("projects"), REFACTORED_PROJECTS("refactored-projects");

	private final String id;

	FileRepositoryCollections(String id) {
		this.id = id;
	}

	public String get() {
		return id;
	}

}
