package br.com.intermediary.intermediaryagent.files.projects;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


import br.com.intermediary.intermediaryagent.files.collections.FileRepositoryCollections;
import br.com.messages.projects.Project;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import static br.com.intermediary.intermediaryagent.utils.Const.PROJECTS;
import static br.com.intermediary.intermediaryagent.utils.Const.REFACTORED_PROJECTS;

@Service
@RequiredArgsConstructor
public class ProjectsGridFsRepositoryImpl implements ProjectsRepository {

	@Qualifier(PROJECTS)
	private final GridFsTemplate gridFsTemplateProjects;

	@Qualifier(REFACTORED_PROJECTS)
	private final GridFsTemplate gridFsTemplateRefactoredProjects;
	private final String SOURCE_FILE_NAME_META = "sourceFileName";


	@Override
	public Optional<Project> get(FileRepositoryCollections collection, String id) {
		var template = this.getTemplate(collection);

		return Optional.ofNullable(template.findOne(new Query(Criteria.where("filename").is(id))))
				.map( f -> this.toProject(f, template));
	}

	@Override
	public Optional<Project> getWithoutContent(FileRepositoryCollections collection, String id) {
		 var template = this.getTemplate(collection);

		return Optional.ofNullable(template.findOne(new Query(Criteria.where("filename").is(id))))
				.map( f -> this.toProject(f, template))
				.map(p -> new Project(p.getId(), p.getName()));
	}

	@Override
	public void put(FileRepositoryCollections collection, Project project) {
		var template = this.getTemplate(collection);
		template.store(project.getStream(), project.getId(), project.getContentType(), Map.of(SOURCE_FILE_NAME_META, project.getName()) );
	}

	@Override
	public void remove(FileRepositoryCollections collection, Project project) {
		var template = this.getTemplate(collection);
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
		return new Project(file.getFilename(),
				file.getMetadata().get(SOURCE_FILE_NAME_META).toString(),
				() -> {
					try {
						return template.getResource(file).getInputStream();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}, file.getMetadata().get("_contentType").toString());
	}
}
