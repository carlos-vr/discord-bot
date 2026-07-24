package com.cvr.discordbot.app.configuration;

import com.cvr.discordbot.core.CleanCommand;
import com.cvr.discordbot.core.MessageEventListener;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * JdaListenerRegister is responsible for registering event listeners and commands with the JDA (Java Discord API) instance.
 * It adds the CleanCommand and MessageEventListener to the JDA instance and registers a slash command for cleaning recent topics.
 *
 * @author carlos
 */
public class JdaListenerRegister {

    public JdaListenerRegister( final JDA jda,
                                 final CleanCommand cleanCommand,
                                 final MessageEventListener messageEventListener ) {
        jda.addEventListener( cleanCommand, messageEventListener );
        jda.upsertCommand( Commands.slash( "clean", "ATENCIÓN: Este comando BORRA todo el contenido del hilo \"Temas recientes\"" )
                .setContexts( InteractionContextType.GUILD ) ).queue();
    }
}
