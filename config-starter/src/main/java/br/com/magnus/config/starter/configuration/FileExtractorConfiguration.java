package br.com.magnus.config.starter.configuration;

import br.com.magnus.config.starter.extractor.S3FileExtractor;
import br.com.magnus.config.starter.extractor.FileExtractor;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileExtractorConfiguration {

    @Bean
    public FileExtractor fileExtractor(S3ProjectRepository s3ProjectRepository) {
        return new S3FileExtractor(s3ProjectRepository);
    }
}
