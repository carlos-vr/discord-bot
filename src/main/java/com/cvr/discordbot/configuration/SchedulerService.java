/**
 * 
 */
package com.cvr.discordbot.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.cvr.discordbot.listeners.ChannelUtils;
import com.cvr.discordbot.listeners.MessageEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

/**
 * @author carlo
 *
 */
@AllArgsConstructor
public class SchedulerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerService.class);

	private static final Marker SCHEDULER_MARKER = MarkerFactory.getMarker("SchedulerService");
	
	private static ObjectMapper mapper = new ObjectMapper();

	private JDA jda;
	
	private MessageEventListener messageEventListener;
	
	private String channel;
	
	private String backupFile;
	
	private HashMap<String, Long> currentAlerts;
	
	public void init() {
		try ( BufferedReader reader = new BufferedReader( new FileReader( backupFile ) ) ){
			String data = reader.readLine();
			LOGGER.warn( SCHEDULER_MARKER, "{} content: {}", backupFile,  data );

			currentAlerts = mapper.readValue( data, HashMap.class );
		}catch (IOException ex) {
			LOGGER.warn( Optional.ofNullable( ex.getMessage()).orElse( ex.toString() ));
			
			currentAlerts = new HashMap<>();
		}
	}
	
	public void destroy() {
		LOGGER.warn( SCHEDULER_MARKER, "SchedulerService.destroy()" );
		saveRecentTopics();
	}
	
	@Scheduled(fixedRate = 10000)
	public void scheduledSave() {
		saveRecentTopics();
	}
	
	private void saveRecentTopics() {  
		if( messageEventListener != null && messageEventListener.getCurrentAlerts() != null) {
			currentAlerts = new HashMap<>( messageEventListener.getCurrentAlerts() );
			
			JSONObject jsonObject = new JSONObject( currentAlerts );
				
			try (BufferedWriter writer = new BufferedWriter( new FileWriter( backupFile ) )){
				writer.write( jsonObject.toString() );
				writer.flush();
			} catch ( IOException e ) {
				LOGGER.error( SCHEDULER_MARKER, "IOException in saveRecentTopics" );
				e.printStackTrace();
			}
		}
	}
	
	@Scheduled(fixedRate = 30000)
	public void scheduledClean() {
		final Map<Long, Message> allMessages;
		
		if( Objects.nonNull( jda ) && Objects.nonNull( jda.getTextChannelById( channel ) ) 
			&& Objects.nonNull( messageEventListener ) && Objects.nonNull( messageEventListener.getCurrentAlerts() ) ) {
			allMessages = ChannelUtils.getMessagesMap(ChannelUtils.getMessages(jda.getTextChannelById( channel)));
			
			if( Objects.nonNull( allMessages ) &&  !allMessages.isEmpty() ) {		
				ChannelUtils.deleteDuplicateMessages( jda.getTextChannelById( channel ), messageEventListener.getCurrentAlerts(), allMessages );
			}
		}
	}
	
}
