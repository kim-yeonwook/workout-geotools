package com.yw;

import com.yw.domain.CRSNameToAuthorityResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkoutGeoToolsApplication {

	static {
		System.setProperty("org.geotools.referencing.forceXY", "true");

		CRSNameToAuthorityResolver.init();
	}

	public static void main(String[] args) {
		SpringApplication.run(WorkoutGeoToolsApplication.class, args);
	}
}
