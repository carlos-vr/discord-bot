/**
 * 
 */
package com.cvr.discordbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.cvr.discordbot.listeners.ChannelUtils;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * @author carlos
 *
 */
public class CleanCommand extends ListenerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(CleanCommand.class);
	public static final Marker COMMAND_MARKER = MarkerFactory.getMarker("COMMAND");
	
	private final String guildId;
	private final String channelId;
	private static final String CLEAN_COMMAND = "clean";
	
	public CleanCommand( final String guildId, final String channelId ) {
		this.guildId = guildId;
		this.channelId = channelId;
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {		
		super.onSlashCommandInteraction( event );				
		
		if ( event.getName().equals( CLEAN_COMMAND ) && guildId.equals( event.getGuild().getId() )
			&& channelId.equals( event.getChannel().getId() )) {
			this.logCommand(LOGGER, event);
			
			ChannelUtils.deleteAllMessagesFromChannel( event.getJDA().getTextChannelById( channelId ) );						
		}
	}
	
	private void logCommand(Logger logger, SlashCommandInteractionEvent event) {
		logger.warn( COMMAND_MARKER, "{} command executed by {} at {}", CLEAN_COMMAND.toUpperCase(), event.getUser().getName().toUpperCase(), event.getTimeCreated() );
	}
}

