package br.com.magnus.metricscalculator.configuration;

import com.github.mauricioaniche.ck.CK;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CkConfiguration {

    @Bean
    @Primary
    public CK ck() {
        return new CK();
    }
}
