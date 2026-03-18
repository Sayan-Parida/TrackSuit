package com.tracksuit.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Load .env BEFORE Spring starts so ${...} placeholders resolve correctly
		loadDotenv();
		SpringApplication.run(BackendApplication.class, args);
	}

	private static void loadDotenv() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("../../")       // TrackSuit root (relative to backend/backend/)
					.ignoreIfMissing()
					.load();

			setIfPresent(dotenv, "GOOGLE_CLIENT_ID");
			setIfPresent(dotenv, "GOOGLE_CLIENT_SECRET");
			setIfPresent(dotenv, "DB_USERNAME");
			setIfPresent(dotenv, "DB_PASSWORD");

			System.out.println("[DotEnv] Loaded .env successfully");
		} catch (Exception e) {
			System.out.println("[DotEnv] No .env found, using system environment variables");
		}
	}

	private static void setIfPresent(Dotenv dotenv, String key) {
		String value = dotenv.get(key);
		if (value != null && !value.isBlank()) {
			System.setProperty(key, value);
		}
	}
}
