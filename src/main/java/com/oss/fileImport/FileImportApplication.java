package com.oss.fileImport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileImportApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileImportApplication.class, args);
	}

}
