package br.com.intermediary.intermediaryagent.datastore;

import br.com.intermediary.intermediaryagent.files.collections.FileRepositoryCollections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import static br.com.intermediary.intermediaryagent.utils.Const.PROJECTS;
import static br.com.intermediary.intermediaryagent.utils.Const.REFACTORED_PROJECTS;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Bean(name = PROJECTS)
    public GridFsTemplate gridFsProjectsTemplate(MappingMongoConverter mappingMongoConverter) {
        return new GridFsTemplate(mongoDbFactory(),mappingMongoConverter, FileRepositoryCollections.PROJECTS.get());
    }

    @Bean(name = REFACTORED_PROJECTS)
    public GridFsTemplate gridFsRefactoredProjectsTemplate(MappingMongoConverter mappingMongoConverter) {
        return new GridFsTemplate(mongoDbFactory(),mappingMongoConverter, FileRepositoryCollections.REFACTORED_PROJECTS.get());
    }

    @Override
    protected String getDatabaseName() {
        return "archprome";
    }
}
