package com.polsl.multimedia.MultimediaProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class MultimediaProjectApplication extends SpringBootServletInitializer {

//	public static void main(String[] args) {
//		SpringApplication.run(MultimediaProjectApplication.class, args);
//	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MultimediaProjectApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MultimediaProjectApplication.class, args);
	}
}
