package com.Mathra.Salon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.Mathra.Salon",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.Mathra\\.Salon\\.service\\.BookingService")
    }
)
public class SalonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalonApplication.class, args);
    }

}