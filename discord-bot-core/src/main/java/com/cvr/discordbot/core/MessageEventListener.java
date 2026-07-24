package com.cvr.discordbot.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Listener for new messages in the Discord server.
 * This class listens for new messages in the Discord server and sends an alert message to a specific channel
 * if the bot has permission to manage the channel where the event happened.
 *
 * @author carlos
 *
 */
@Slf4j
@RequiredArgsConstructor
public class MessageEventListener extends ListenerAdapter{

    private final ChannelService channelService;
    private final String guildId;
	private final String channelId;
	private final List<String> restrictedChannels;
	private final EmbedBuilder embedBuilder;
	private final Map<String, Long> currentAlerts;
	
	/**
	 *	Handler for new message event.
	 *
	 *	If the bot has permits to manage the channel where the event happened, it delete (if exists) the last alert
	 *	coming from that same channel in "Recent Topics" channel and sends a new alert message to it.
	 *
	 *	Updates the currentAlerts map too. 
	 *
	 */
	@Override
	public void onMessageReceived( MessageReceivedEvent event ) {		
		super.onMessageReceived( event );		
		
		if( this.guildId.equals( event.getGuild().getId() )) {
			boolean sendMessagesPermit = false;
		
			final String mensaje = buildMessage( event.getMessage() ); 			
		
			final TextChannel textChannel = event.getJDA().getTextChannelById( this.channelId );

            if( textChannel == null ) {
                log.error( "TextChannel {} not found", this.channelId );
                return;
            }

			sendMessagesPermit = textChannel.canTalk() && !this.restrictedChannels.contains( event.getChannel().getId() );
				
			if( this.currentAlerts.get( event.getChannel().getId() ) != null ) {
                channelService.deleteMessage( this.currentAlerts.get( event.getChannel().getId() ), false );
			}		
		
			if( sendMessagesPermit ) {
				List< MessageEmbed > embeds = new ArrayList<>();
				embedBuilder.setColor( new Color(0, 153, 0) );		
				embedBuilder.setDescription( mensaje );
				embeds.add( embedBuilder.build() );

                channelService.sendMessage( embeds, currentAlerts, event.getChannel().getId() );
			}
		}
	}

    /**
     * Returns the current alerts map.
     *
     * @return The current alerts map.
     */
	public Map<String, Long> getCurrentAlerts(){
		return this.currentAlerts;
	}
	
	/**
	 * Builds a message with the author's nickname if present or the author's name otherwise and the URL of the channel where the new comment was added.
	 * 
	 * @param message The new message content
	 * @return The content of the alert message that will be added in the "Recent Topics" channel
	 */
	private String buildMessage( final Message message) {
		String author = Optional.ofNullable( message.getMember() )
				.filter( m -> Objects.nonNull( m.getNickname() ))
				.map( Member::getNickname)
				.orElse( message.getAuthor().getName() );
		
		return "**" + author + "**" + " publicó en " + message.getJumpUrl();
	}
}
