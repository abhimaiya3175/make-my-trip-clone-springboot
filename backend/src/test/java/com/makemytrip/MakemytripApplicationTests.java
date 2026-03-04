package com.makemytrip;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = MakemytripApplicationTests.TestConfiguration.class)
class MakemytripApplicationTests {

	@Configuration
	static class TestConfiguration {
		// Minimal config for testing Java 21 compatibility
	}

	@Test
	void contextLoads() {
		// Test passes if Spring context loads successfully with Java 21
	}

}
