package br.com.magnus.metricscalculator.configuration;

import com.github.mauricioaniche.ck.CK;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CkConfiguration {

    @Bean
    public CK ck() {
        return new CK();
    }
}
