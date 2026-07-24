# discord-bot

Bot de utilidades para el servidor de Discord **PdF**, desarrollado con **Java 17**, **Spring Boot** y **JDA (Java Discord API)**.

## ¿Qué hace?

- **Temas Recientes**: publica automáticamente en un canal dedicado un resumen de los últimos mensajes de los canales activos del servidor.
- **Limpieza de duplicados**: detecta y elimina mensajes duplicados en el canal de temas recientes mediante un scheduler.
- **Backup**: persiste el estado del bot en un fichero para sobrevivir a reinicios.
- **Comando `/clean`**: permite limpiar manualmente el canal de temas recientes.

## Estructura del proyecto

El proyecto es un **multi-módulo Maven**:

```
discord-bot/
├── discord-bot-app/       # Módulo principal: configuración Spring Boot, schedulers y arranque
│   └── configuration/
│       ├── DiscordBotConfiguration.java       # Definición de beans (JDA, servicios, listeners)
│       ├── DiscordBotBackupScheduler.java      # Scheduler de limpieza y backup
│       ├── DiscordBotExternalProperties.java   # Propiedades externas (token, IDs de canales...)
│       └── JdaListenerRegister.java            # Registro de listeners en JDA
│
├── discord-bot-core/      # Lógica de negocio
│   └── core/
│       ├── ChannelService.java         # Operaciones sobre canales (leer, enviar, borrar mensajes)
│       ├── MessageEventListener.java   # Escucha eventos de mensajes de Discord
│       ├── CleanCommand.java           # Comando slash /clean
│       └── BackupService.java          # Lectura y escritura del fichero de backup
│
└── discord-bot-model/     # Modelos compartidos entre módulos
```

## Tecnologías

- Java 17
- Spring Boot 3.5.x
- Spring Cloud Config (config-server externo)
- JDA 6.x (Java Discord API)
- Maven (multi-módulo)
- Lombok

## Configuración

El bot se configura mediante un **config-server** externo. Las propiedades necesarias son:

```yaml
discord:
  token: <token del bot>          # Token del bot de Discord (determina el servidor al que conecta)
  server-id: <id del servidor>
  temas-recientes: <id del canal> # Canal donde se publican los temas recientes
  mod-channels:                   # Canales de moderación (ignorados para temas recientes)
    - <id>
  ignored-channels:               # Canales ignorados adicionales
    - <id>
  backup-file: <ruta del fichero> # Ruta del fichero de backup
```

> ⚠️ El token determina a qué servidor conecta el bot. Usa el token correcto para cada entorno (pruebas/producción).