/**
 * 
 */
package com.cvr.discordbot.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Service that handles the clean command, which deletes all messages in a specific channel.
 *
 * @author carlos
 *
 */
@Slf4j
@RequiredArgsConstructor
public class CleanCommand extends ListenerAdapter {
    
	public static final Marker COMMAND_MARKER = MarkerFactory.getMarker( "COMMAND" );

    private final ChannelService channelService;
	private final String guildId;
	private final String channelId;
	private static final String CLEAN_COMMAND = "clean";

    /**
     * Handles the slash command interaction event for the clean command.
     *
     * @param event The slash command interaction event.
     */
	@Override
	public void onSlashCommandInteraction( final SlashCommandInteractionEvent event ) {
		super.onSlashCommandInteraction( event );				
		
		if ( event.getName().equals( CLEAN_COMMAND ) && guildId.equals( event.getGuild().getId() )
			&& channelId.equals( event.getChannel().getId() )) {
            log.warn( COMMAND_MARKER, "{} command executed by {} at {}", CLEAN_COMMAND.toUpperCase(), event.getUser().getName().toUpperCase(), event.getTimeCreated() );

            event.deferReply( true ).queue();

            channelService.deleteAllMessages();
		}
	}
}
