package com.cvr.discordbot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cvr.discordbot")
public class DiscordBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordBotApplication.class, args);
	}

}
