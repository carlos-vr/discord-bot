package com.cvr.discordbot.listeners;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

/**
 * @author carlos
 *
 */
public class ChannelUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelUtils.class);
	public static final Marker removeMessage = MarkerFactory.getMarker("RemoveMessage");
	public static final Marker insertMessage = MarkerFactory.getMarker("InsertMessage");	
	
	public static List<Message> getMessages( final StandardGuildMessageChannel channel ) {
		final int limit = 100;
		int elements = 0;
		
		final List< Message > messages = new ArrayList<>( MessageHistory.getHistoryFromBeginning( channel ).limit( limit ).complete()
				.getRetrievedHistory() );
		
		Collections.reverse(messages);
		
		elements = messages.size();
		while( elements > 0) {
			List<Message> aux = new ArrayList<>( MessageHistory.getHistoryAfter( channel, messages.get( messages.size() - 1 ).getId() ).limit( limit ).complete()
				.getRetrievedHistory() );
			
			if( !aux.isEmpty()) {
			Collections.reverse( aux );			
				aux.forEach( messages::add);			
			}			
			elements = aux.size();
		}
		return messages;		
	}
	
	public static Map<Long, Message> getMessagesMap( final List< Message > messages ){		
		return Optional.ofNullable( messages ).orElse( new ArrayList<>() ).stream()
			.collect( 
				Collectors.toMap( Message::getIdLong, Function.identity() ) 
			);				
	}

	public static void deleteDuplicateMessages( final StandardGuildMessageChannel channel, final Map<String, Long> ids,
			Map<Long, Message> candidates) {
		List<OffsetDateTime> timestamps = new ArrayList<>();
		
		Optional.ofNullable( ids ).orElse( new HashMap<>() ).keySet().forEach( 
				id -> {
					if( candidates.get( ids.get( id ) ) != null ) {						
						final Message m = candidates.get( ids.get( id ) );
						
						timestamps.add( m.getTimeCreated() );						
						
						// Eliminamos los que existen en la lista ids dejando unicamente aquellos que no lo estan
						candidates.remove( m.getIdLong() );
					}
				}
		);
		
		Collections.sort(timestamps, Collections.reverseOrder());
		
		if( !timestamps.isEmpty()) {
			Optional.ofNullable( candidates ).orElse( new HashMap<>() ).keySet().forEach( 
				id -> {
					if( candidates.get( id ).getTimeCreated().isBefore( timestamps.get( 0 ) ) ) {
						deleteMessage( channel, id, true );
					}
				});
		}
	}
	
	/**
	 * Deletes the message identified by ID from the specified channel.
	 * 
	 * @param channel The channel from which messages will be deleted
	 * @param id The message ID to be deleted
	 * 
	 */
	public static void deleteMessage( final StandardGuildMessageChannel channel, final Long id, boolean log) {
		channel.deleteMessageById( id ).queue( 
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
							error.printStackTrace();
						}
					}else {
						LOGGER.error( removeMessage, "Error deleting message {}", id );
						error.printStackTrace();	
					}										
				
				}
		);
	}
	
	/**
	 * Delete all messages from channel
	 * 
	 * @param channel The channel from which the messages will be deleted
	 */
	public static void deleteAllMessagesFromChannel( final StandardGuildMessageChannel channel ) {
		getMessages( channel ).forEach(
			m -> ChannelUtils.deleteMessage( channel, m.getIdLong(), false )
		);
	}	
	
	/**
	 * Send a new message to the textChannel.
	 * 
	 * @param textChannel The channel where the message will be added
	 * @param embeds The message content
	 * @param lastComments
	 * @param channelId
	 */
	public static void sendMessage( final TextChannel textChannel, final List< MessageEmbed > embeds,
			final Map<String, Long> lastComments, final String channelId ) {
		textChannel.sendMessageEmbeds( embeds ).queue( 
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

}

