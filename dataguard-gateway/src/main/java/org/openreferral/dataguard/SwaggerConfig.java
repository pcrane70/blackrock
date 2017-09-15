package org.openreferral.dataguard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import static springfox.documentation.builders.PathSelectors.regex;
 
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.openreferral.dataguard"))
                .paths(regex("/dataguard.*"))
                .build()
                .apiInfo(metaData());
    }
    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "OpenReferral Steward Gateway API",
                "APIs for Openreferral Repository Management",
                "1.0",
                "Terms of service",
                new Contact("Greg Bloom", "https://openreferral.readthedocs.io/en/latest/credits/", "@greggish"),
               "License Version and Governance",
                "https://openreferral.readthedocs.io/en/latest/governance/");
        return apiInfo;
    }
}
