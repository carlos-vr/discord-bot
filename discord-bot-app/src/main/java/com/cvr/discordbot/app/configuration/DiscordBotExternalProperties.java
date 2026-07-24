package com.cvr.discordbot.app.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * DiscordBotExternalProperties is a configuration class that holds external properties for the Discord bot application.
 * It uses Spring Boot's @ConfigurationProperties to bind properties from application configuration files.
 * The class contains fields for the bot token, server ID, recent topics channel, moderator channels, ignored channels, and backup file path.
 *
 * @author carlos
 */
@Configuration
@ConfigurationProperties
@Data
public class DiscordBotExternalProperties {

    private String token;
    private String serverId;
    private String temasRecientes;
    private List<String> modChannels;
    private List<String> ignoredChannels;
    private String backupFile;
}
