package br.com.messages.configuration;

import br.com.messages.files.FileRepositoryCollections;
import br.com.messages.projects.ProjectsRepository;
import br.com.messages.projects.ProjectsRepositoryImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@AutoConfiguration
public class DataStoreConfiguration extends AbstractMongoClientConfiguration {

    private GridFsTemplate gridFsProjectsTemplate(MappingMongoConverter mappingMongoConverter) {
        return new GridFsTemplate(mongoDbFactory(),mappingMongoConverter, FileRepositoryCollections.PROJECTS.get());
    }

    private GridFsTemplate gridFsRefactoredProjectsTemplate(MappingMongoConverter mappingMongoConverter) {
        return new GridFsTemplate(mongoDbFactory(),mappingMongoConverter, FileRepositoryCollections.REFACTORED_PROJECTS.get());
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectsRepository projectsRepository(MappingMongoConverter mappingmongoconverter) {
        return new ProjectsRepositoryImpl(gridFsRefactoredProjectsTemplate(mappingmongoconverter), gridFsProjectsTemplate(mappingmongoconverter));
    }

    @Override
    protected String getDatabaseName() {
        return "archprome";
    }
}
