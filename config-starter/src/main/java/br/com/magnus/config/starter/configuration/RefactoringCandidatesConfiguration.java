package br.com.magnus.config.starter.configuration;

import br.com.magnus.config.starter.members.candidates.BasicRefactoringCandidate;
import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefactoringCandidatesConfiguration {

    @Bean
    @ConditionalOnMissingBean(RefactoringCandidate.class)
    public RefactoringCandidate basicRefactoringCandidate() {
        return new BasicRefactoringCandidate();
    }

}
