/**
 * 
 */
package com.cvr.discordbot.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.cvr.discordbot.commands.CleanCommand;
import com.cvr.discordbot.listeners.MessageEventListener;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 * @author carlo
 *
 */
@Configuration
@EnableScheduling
public class DiscordBotConfiguration {

	@Value("${token}")
	private String token;
	
	@Value("${serverId}")
	private String serverId;
	
	@Value("${temasRecientes}")
	private String temasRecientes;
	
	@Value("#{'${modsChannels}'.split(',')}")
	private List<String> modsChannels;
	
	@Value("#{'${tagsChannels}'.split(',')}")
	private List<String> skippedChannels;
	
	@Value("${backupFile}")
	private String backupFile;
    
    @Bean("cleanCommand")
    CleanCommand cleanCommand() {
    	return new CleanCommand( serverId, temasRecientes );
    }
    
    @Bean("messageEventListener")
    MessageEventListener messageEventListener() {
    	// Mod channels and text channels with bot football results will be skipped for "Recent topics"
    	List<String> restrictedChannels = new ArrayList<>();
    	restrictedChannels.addAll( modsChannels );
    	restrictedChannels.addAll( skippedChannels );
    	
    	// Add the Recent topic channel too in skipped list
    	restrictedChannels.add( temasRecientes ); 
    	
    	return new MessageEventListener( serverId, temasRecientes, restrictedChannels, new HashMap<>() );
    }
    
    @Bean("jda")
    JDA getApi( @Qualifier( "cleanCommand" ) final CleanCommand cleanCommand, @Qualifier( "messageEventListener" ) final MessageEventListener messageEventListener) {
    	JDA jda = JDABuilder
    			.createDefault( token )
    			.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
    			.addEventListeners( messageEventListener, cleanCommand )
    			.build();		
    		
    	jda.upsertCommand("clean", "ATENCIÓN: Este comando BORRA todo el contenido del hilo \"Temas recientes\"").setGuildOnly(true).queue();
    	
    	return jda;
    }
    
    @Bean(name="scheduler", initMethod="init", destroyMethod="destroy")
    SchedulerService schedulerService(@Qualifier("jda") final JDA jda, @Qualifier( "messageEventListener" ) final MessageEventListener messageEventListener ) {
		return new SchedulerService( jda, messageEventListener, temasRecientes, backupFile, new HashMap<>() );    	
    }
}
