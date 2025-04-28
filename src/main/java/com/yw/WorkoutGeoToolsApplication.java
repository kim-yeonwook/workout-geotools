package com.yw;

import com.yw.domain.AuthorityResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkoutGeoToolsApplication {

	static {
		System.setProperty("org.geotools.referencing.forceXY", "true");

		AuthorityResolver.init();
	}

	public static void main(String[] args) {
		SpringApplication.run(WorkoutGeoToolsApplication.class, args);
	}
}
