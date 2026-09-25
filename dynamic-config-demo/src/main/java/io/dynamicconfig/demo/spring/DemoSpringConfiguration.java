package io.dynamicconfig.demo.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DynamicConfigInfrastructure.class)
@ComponentScan(basePackageClasses = AppSettings.class)
public class DemoSpringConfiguration {
}
