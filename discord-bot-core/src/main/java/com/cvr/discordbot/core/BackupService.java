/**
 * 
 */
package com.cvr.discordbot.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class to handle backup operations for recent topics in a Discord bot application.
 * It provides methods to load and save recent topics to a specified backup file.
 * The class uses Jackson's ObjectMapper for JSON serialization and deserialization,
 * and it logs relevant information using SLF4J.
 *
 * @author carlos
 *
 */
@Slf4j
@RequiredArgsConstructor
public class BackupService {

	private static final Marker BACKUP_SERVICE_MARKER = MarkerFactory.getMarker( "BackupService" );
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private final String backupFile;

    /**
     * Loads recent topics from the backup file.
     * If the file is not found or an error occurs during reading, it returns an empty map.
     *
     * @return a map containing recent topics and their corresponding timestamps
     */
    public Map< String, Long > loadRecentTopics() {
        try ( BufferedReader reader = new BufferedReader( new FileReader( backupFile ) ) ) {
            String data = reader.readLine();
            log.warn( BACKUP_SERVICE_MARKER, "{} content: {}", backupFile, data );
            return mapper.readValue(data, HashMap.class);
        } catch ( final IOException ex ) {
            log.warn( Optional.ofNullable( ex.getMessage() ).orElse(ex.toString() ) );
            return new HashMap<>();
        }
    }

    /**
     * Saves recent topics to the backup file.
     * If the provided map is null, the method returns without performing any operation.
     *
     * @param alerts a map containing recent topics and their corresponding timestamps
     */
	public void saveRecentTopics( final Map<String, Long> alerts ) {
        if (alerts == null) return;

        JSONObject jsonObject = new JSONObject(alerts);

        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( backupFile ) ) ) {
            writer.write( jsonObject.toString() );
            writer.flush();
        } catch ( final IOException e ) {
            log.error( BACKUP_SERVICE_MARKER, "IOException in saveRecentTopics", e );
        }
	}

}
