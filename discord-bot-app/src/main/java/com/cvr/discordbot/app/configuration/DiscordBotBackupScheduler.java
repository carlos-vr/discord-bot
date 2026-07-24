package com.cvr.discordbot.app.configuration;

import com.cvr.discordbot.core.BackupService;
import com.cvr.discordbot.core.ChannelService;
import com.cvr.discordbot.core.MessageEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * DiscordBotBackupScheduler is responsible for scheduling backup and cleanup tasks for the Discord bot.
 * It uses the BackupService to save recent topics and the ChannelService to delete duplicate messages.
 * The scheduled tasks run at fixed intervals defined by the @Scheduled annotation.
 *
 * @author carlos
 */
@Slf4j
@RequiredArgsConstructor
public class DiscordBotBackupScheduler {

    private static final Marker BACKUP_SCHEDULER_MARKER = MarkerFactory.getMarker("BackupScheduler");

    private final BackupService backupService;
    private final ChannelService channelService;
    private final MessageEventListener messageEventListener;

    public void init() {
        log.warn( BACKUP_SCHEDULER_MARKER, "BackupScheduler initialized." );
    }

    public void destroy() {
        log.warn( BACKUP_SCHEDULER_MARKER, "BackupScheduler.destroy()" );
        backupService.saveRecentTopics( messageEventListener.getCurrentAlerts() );
    }

    @Scheduled( fixedRate = 1800000 )
    public void scheduledSave() {
        log.warn( "Saving backup..." );
        backupService.saveRecentTopics( messageEventListener.getCurrentAlerts() );
        log.warn( "Backup saved successfully." );
    }

    @Scheduled( fixedRate = 1000000 )
    public void scheduledClean() {
        log.warn( "Trying to clean duplicates..." );
        channelService.deleteDuplicateMessages( messageEventListener.getCurrentAlerts() );
        log.warn( "Cleaning duplicates finished." );
    }

}
