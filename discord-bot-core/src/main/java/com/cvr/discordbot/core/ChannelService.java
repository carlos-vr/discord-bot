package com.cvr.discordbot.core;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class for managing messages in a Discord channel using JDA (Java Discord API).
 *
 * @author carlos
 *
 */
@RequiredArgsConstructor
public class ChannelService {

	private static final Logger LOGGER = LoggerFactory.getLogger( ChannelService.class );
	public static final Marker removeMessage = MarkerFactory.getMarker( "RemoveMessage" );
	public static final Marker insertMessage = MarkerFactory.getMarker( "InsertMessage" );

    private final JDA jda;
    private final String channelId;

    /**
     * Retrieves all messages from the specified channel, starting from the beginning and continuing until no more messages are found.
     *
     * @return A list of messages in the channel, ordered from oldest to newest.
     */
	public List<Message> getMessages() {
		final int limit = 100;
		int elements = 0;
		
		final List< Message > messages = new ArrayList<>( MessageHistory.getHistoryFromBeginning( getChannel() ).limit( limit ).complete()
				.getRetrievedHistory() );
		
		Collections.reverse(messages);
		
		elements = messages.size();
		while( elements > 0) {
			List<Message> aux = new ArrayList<>( MessageHistory.getHistoryAfter( getChannel(), messages.get( messages.size() - 1 ).getId() ).limit( limit ).complete()
				.getRetrievedHistory() );
			
			if( !aux.isEmpty()) {
			Collections.reverse( aux );			
				aux.forEach( messages::add);			
			}			
			elements = aux.size();
		}
		return messages;		
	}

    /**
     * Converts a list of messages into a map where the key is the message ID and the value is the message itself.
     *
     * @param messages The list of messages to be converted into a map.
     * @return A map containing message IDs as keys and corresponding messages as values.
     */
	public Map<Long, Message> getMessagesMap( final List< Message > messages ){
		return Optional.ofNullable( messages ).orElse( new ArrayList<>() ).stream()
			.collect( 
				Collectors.toMap( Message::getIdLong, Function.identity() ) 
			);				
	}

    /**
     * Deletes duplicate messages from the specified channel based on the provided map of message IDs.
     * The method identifies messages that have timestamps earlier than the most recent message and deletes them.
     *
     * @param ids A map containing message IDs to be checked for duplicates.
     */
	public void deleteDuplicateMessages( final Map<String, Long> ids  ) {
        final Map<Long, Message> candidates = getMessagesMap( getMessages() );
        final List<OffsetDateTime> timestamps = new ArrayList<>();

        Optional.ofNullable( ids ).orElse( new HashMap<>() ).keySet().forEach(
                id -> {
                    if (candidates.get( ids.get( id ) ) != null ) {
                        final Message m = candidates.get( ids.get( id ) );
                        timestamps.add( m.getTimeCreated() );
                        candidates.remove( m.getIdLong() );
                    }
                }
        );

        Collections.sort( timestamps, Collections.reverseOrder() );

        if ( !timestamps.isEmpty() ) {
            Optional.ofNullable( candidates ).orElse( new HashMap<>() ).keySet().forEach(
                    id -> {
                        if ( candidates.get( id ).getTimeCreated().isBefore( timestamps.get( 0 ) ) ) {
                            deleteMessage( id, true );
                        }
                    });
        }
    }
	
	/**
	 * Deletes the message identified by ID from the specified channel.
	 *
	 * @param id The message ID to be deleted
	 * 
	 */
	public void deleteMessage( final Long id, final boolean log ) {
        getChannel().deleteMessageById( id ).queue(
				successDelete -> {
					if( log ) {
						LOGGER.warn( removeMessage, "Message {} deleted.", id);
					}
				},
				error -> { 
					if ( error instanceof ErrorResponseException errorResponseException) { 
						ErrorResponse responseError = errorResponseException.getErrorResponse();
						LOGGER.error( removeMessage, "Error deleting message {} Error: {}", id, responseError );
								
						if( responseError.getCode() != ErrorResponse.UNKNOWN_MESSAGE.getCode()) {
                            LOGGER.error( removeMessage, "Unexpected error {}", id, error );
						}
					}else {
						LOGGER.error( removeMessage, "Error deleting message {}", id, error );
					}										
				
				}
		);
	}
	
	/**
	 * Delete all messages from channel
     *
	 */
	public void deleteAllMessages() {
		getMessages().forEach(
			m -> deleteMessage( m.getIdLong(), false )
		);
	}	
	
	/**
	 * Send a new message to the textChannel (temasRecientes) with the content of the embeds list.
     * After sending the message, it updates the lastComments map with the new message ID for the specified channelId.
	 *
	 * @param embeds The message content
	 * @param lastComments
	 * @param channelId
	 */
	public void sendMessage( final List< MessageEmbed > embeds,
			final Map<String, Long> lastComments, final String channelId ) {
        getChannel().sendMessageEmbeds( embeds ).queue(
				message -> {		
					long newMessageId = message.getIdLong();
					
					// Add the new message ID of "Recent Topics" in lastComments Map ( The key is the ChannelId where the comment was added )
					lastComments.put( channelId, newMessageId );
				},
				error -> {
					if ( error instanceof ErrorResponseException errorResponseException) {														
						ErrorResponse responseError = errorResponseException.getErrorResponse();
												
						LOGGER.error( insertMessage, "Error sending alert for update in {} channel. Error: {}", channelId, responseError );
						if( responseError.getCode() != ErrorResponse.UNKNOWN_MESSAGE.getCode()) {
							error.printStackTrace();
						}
					}else {
						LOGGER.error( insertMessage, "Error sending alert for update in {} channel.", channelId );
						error.printStackTrace();	
					}										
			});
	}

    /**
     * Retrieves the text channel associated with the specified channel ID.
     * This method waits for the JDA instance to be ready before attempting to retrieve the channel.
     *
     * @return The StandardGuildMessageChannel corresponding to the specified channel ID.
     * @throws IllegalStateException if the JDA instance is interrupted while waiting for readiness.
     */
    private StandardGuildMessageChannel getChannel() {
        try {
            jda.awaitReady();
        } catch ( final InterruptedException e ) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException( "JDA interrupted while waiting for ready", e );
        }
        return jda.getTextChannelById( channelId );
    }

}
