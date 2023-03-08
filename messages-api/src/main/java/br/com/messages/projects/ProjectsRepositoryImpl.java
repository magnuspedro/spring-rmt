package br.com.messages.projects;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import br.com.messages.files.FileRepositoryCollections;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@RequiredArgsConstructor
public class ProjectsRepositoryImpl implements ProjectsRepository {

	private final GridFsTemplate gridFsTemplateProjects;

	private final GridFsTemplate gridFsTemplateRefactoredProjects;
	private final String SOURCE_FILE_NAME_META = "sourceFileName";


	@Override
	public Optional<Project> get(FileRepositoryCollections collection, String id) {
		final GridFsTemplate template = this.getTemplate(collection);

		return Optional.ofNullable(template.findOne(new Query(Criteria.where("filename").is(id))))
				.map( f -> this.toProject(f, template));
	}

	@Override
	public Optional<Project> getWithoutContent(FileRepositoryCollections collection, String id) {
		final GridFsTemplate template = this.getTemplate(collection);

		return Optional.ofNullable(template.findOne(new Query(Criteria.where("filename").is(id))))
				.map( f -> this.toProject(f, template))
				.map(p -> Project.builder()
						.id(p.getId())
						.name(p.getName())
						.build());
	}

	@Override
	public void put(FileRepositoryCollections collection, Project project) {
		final GridFsTemplate template = this.getTemplate(collection);
		Map<String, String> metadata = new java.util.HashMap<>();
		metadata.put(SOURCE_FILE_NAME_META, project.getName());

		template.store(project.getStream(), project.getId(), project.getContentType(), metadata);
	}

	@Override
	public void remove(FileRepositoryCollections collection, Project project) {
		final GridFsTemplate template = this.getTemplate(collection);
		template.delete(new Query(Criteria.where("filename").is(project.getId())));
	}
private GridFsTemplate getTemplate(FileRepositoryCollections collection){
	if(collection.equals(FileRepositoryCollections.PROJECTS)){
		return gridFsTemplateProjects;
	}
	return gridFsTemplateRefactoredProjects;
}

	@SneakyThrows
	private Project toProject(GridFSFile file, GridFsTemplate template) {
		return	Project.builder()
				.id(file.getFilename())
				.name(file.getMetadata().get(SOURCE_FILE_NAME_META).toString())
				.contentHandler(() -> {
					try {
						return template.getResource(file).getInputStream();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				})
				.contentType(file.getMetadata().get("_contentType").toString())
				.build();
	}
}
