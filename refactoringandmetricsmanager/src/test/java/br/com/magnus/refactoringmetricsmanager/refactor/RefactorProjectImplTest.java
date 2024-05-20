package br.com.magnus.refactoringmetricsmanager.refactor;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.file.extractor.FileExtractor;
import br.com.magnus.config.starter.projects.Project;
import br.com.magnus.config.starter.repository.S3ProjectRepository;
import br.com.magnus.refactoringandmetricsmanager.gateway.SendProject;
import br.com.magnus.refactoringandmetricsmanager.refactor.RefactorProjectImpl;
import br.com.magnus.refactoringandmetricsmanager.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefactorProjectImplTest {

    @Mock
    private S3ProjectRepository s3ProjectRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private SendProject sendProject;
    @Mock
    private FileExtractor fileExtractor;

    private BucketProperties bucket;
    private RefactorProjectImpl refactorProject;

    @BeforeEach
    void setup() {

        this.bucket = new BucketProperties();
        this.bucket.setProjectBucket("bucket");
        this.refactorProject = new RefactorProjectImpl(s3ProjectRepository, projectRepository, sendProject, bucket, fileExtractor);
    }

    @Test
    @DisplayName("Should test project saving and sending to queue")
    void ShouldTestProjectSavingAndSendingToQueue() {
        var project = Project.builder()
                .id("id")
                .contentType("String")
                .zipContent("Content".getBytes())
                .build();

        this.refactorProject.process(project);

        verify(this.s3ProjectRepository, atLeastOnce()).upload(eq(bucket.getProjectBucket()), eq(project.getId()), any(InputStream.class), assertArg(it ->
                assertThat(it.getContentType(), is(project.getContentType()))
        ));
        verify(this.projectRepository).save(project);
        verify(this.sendProject).send(project);
    }

}
