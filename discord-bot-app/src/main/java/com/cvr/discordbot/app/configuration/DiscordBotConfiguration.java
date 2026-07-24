/**
 * 
 */
package com.cvr.discordbot.app.configuration;

import com.cvr.discordbot.core.BackupService;
import com.cvr.discordbot.core.ChannelService;
import com.cvr.discordbot.core.CleanCommand;
import com.cvr.discordbot.core.MessageEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration class for the Discord bot application.
 * It defines beans for various components such as commands, event listeners, JDA instance, backup service, and channel service.
 * The configuration also enables scheduling for periodic tasks.
 *
 * @author carlos
 *
 */
@Configuration
@EnableScheduling
public class DiscordBotConfiguration {

    /**
     * Creates a CleanCommand bean that is responsible for cleaning duplicate messages in the Discord server.
     *
     * @param channelService the ChannelService used to interact with Discord channels
     * @param discordBotExternalProperties the external properties containing configuration values
     * @return a new instance of CleanCommand
     */
    @Bean("cleanCommand")
    CleanCommand cleanCommand( @Qualifier( "channelService" ) final ChannelService channelService,
                               final DiscordBotExternalProperties discordBotExternalProperties) {
    	return new CleanCommand( channelService, discordBotExternalProperties.getServerId(),
                discordBotExternalProperties.getTemasRecientes() );
    }

    /**
     * Creates a MessageEventListener bean that listens for message events in the Discord server.
     * It filters out messages from restricted channels and maintains a map of recent topics.
     *
     * @param channelService the ChannelService used to interact with Discord channels
     * @param discordBotExternalProperties the external properties containing configuration values
     * @param backupService the BackupService used to load recent topics from backup
     * @return a new instance of MessageEventListener
     */
    @Bean("messageEventListener")
    MessageEventListener messageEventListener( @Qualifier( "channelService" ) final ChannelService channelService,
                                                final DiscordBotExternalProperties discordBotExternalProperties,
    final BackupService backupService ) {
    	// Mod channels and text channels with bot football results will be skipped for "Recent topics"
    	List<String> restrictedChannels = new ArrayList<>();
    	restrictedChannels.addAll( discordBotExternalProperties.getModChannels() );
    	restrictedChannels.addAll( discordBotExternalProperties.getIgnoredChannels() );
    	
    	// Add the Recent topic channel too in skipped list
    	restrictedChannels.add( discordBotExternalProperties.getTemasRecientes() );

        return new MessageEventListener( channelService, discordBotExternalProperties.getServerId(), discordBotExternalProperties.getTemasRecientes(), restrictedChannels, new EmbedBuilder(), new ConcurrentHashMap<>( backupService.loadRecentTopics() ) );
    }

    /**
     * Creates a JDA (Java Discord API) instance bean that connects to the Discord server using the provided token.
     * It enables the necessary gateway intents for message content and guild messages.
     *
     * @param discordBotExternalProperties the external properties containing configuration values
     * @return a new instance of JDA
     */
    @Bean("jda")
    JDA getApi( final DiscordBotExternalProperties discordBotExternalProperties ) {
    	final JDA jda = JDABuilder
    			.createDefault( discordBotExternalProperties.getToken() )
    			.enableIntents( GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES )
    			.build();
    	return jda;
    }

    /**
     * Creates a BackupService bean that handles saving and loading recent topics to and from a backup file.
     *
     * @param discordBotExternalProperties the external properties containing configuration values
     * @return a new instance of BackupService
     */
    @Bean( name = "backupSchedulerService" )
    BackupService backupSchedulerService( final DiscordBotExternalProperties discordBotExternalProperties ) {
		return new BackupService( discordBotExternalProperties.getBackupFile() );
    }

    /**
     * Creates a DiscordBotBackupScheduler bean that schedules backup and cleanup tasks for the Discord bot.
     * It uses the BackupService to save recent topics and the ChannelService to delete duplicate messages.
     *
     * @param backupService the BackupService used to save recent topics
     * @param channelService the ChannelService used to delete duplicate messages
     * @param messageEventListener the MessageEventListener that maintains recent topics
     * @return a new instance of DiscordBotBackupScheduler
     */
    @Bean( name = "backupScheduler", initMethod = "init", destroyMethod = "destroy" )
    public DiscordBotBackupScheduler backupScheduler(final BackupService backupService, final ChannelService channelService, final MessageEventListener messageEventListener) {
        return new DiscordBotBackupScheduler( backupService, channelService, messageEventListener );
    }

    /**
     * Creates a ChannelService bean that provides methods to interact with Discord channels.
     * It waits for the JDA instance to be ready before creating the service.
     *
     * @param jda the JDA instance used to interact with Discord
     * @param discordBotExternalProperties the external properties containing configuration values
     * @return a new instance of ChannelService
     * @throws InterruptedException if the thread is interrupted while waiting for JDA to be ready
     */
    @Bean( "channelService" )
    ChannelService channelService( final JDA jda, final DiscordBotExternalProperties discordBotExternalProperties ) throws InterruptedException {
        jda.awaitReady(); // espera a que JDA esté conectado
    	return new ChannelService( jda, discordBotExternalProperties.getTemasRecientes() );
    }

    /**
     * Creates a JdaListenerRegister bean that registers event listeners with the JDA instance.
     * It registers the CleanCommand and MessageEventListener to listen for events in the Discord server.
     *
     * @param jda the JDA instance used to interact with Discord
     * @param cleanCommand the CleanCommand that listens for cleanup commands
     * @param messageEventListener the MessageEventListener that listens for message events
     * @return a new instance of JdaListenerRegister
     */
    @Bean( name = "jdaListenerRegister" )
    public JdaListenerRegister jdaListenerRegister( @Qualifier( "jda" ) final JDA jda, @Qualifier( "cleanCommand" ) final CleanCommand cleanCommand, @Qualifier( "messageEventListener" ) final MessageEventListener messageEventListener ) {
        return new JdaListenerRegister( jda, cleanCommand, messageEventListener );
    }
}
