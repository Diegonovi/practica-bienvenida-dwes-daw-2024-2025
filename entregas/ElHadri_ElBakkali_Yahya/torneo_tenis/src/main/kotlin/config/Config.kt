package config

import org.lighthousegames.logging.logging
import java.nio.file.Files
import java.util.*
import kotlin.io.path.Path


private val logger = logging()


object Config {
    var databaseUrl: String = "jdbc:sqlite:torneo.db"
        private set
    var storageData: String = "data"
        private set
    var cacheSize: Int = 5
        private set

    init {
        try {
            logger.debug { "Cargando configuración DataBaseManager" }
            val properties = Properties()
            properties.load(ClassLoader.getSystemResourceAsStream("config.properties"))
            databaseUrl = properties.getProperty("database.url", this.databaseUrl)
            storageData = properties.getProperty("storage.data", this.storageData)
            cacheSize = properties.getProperty("cache.size", this.cacheSize.toString()).toInt()
            logger.debug { "Configuración cargada correctamente" }

            Files.createDirectories(Path(storageData))
        } catch (e: Exception) {
            logger.error { "Error cargando configuración: ${e.message}" }
            logger.info { "Usando valores por defecto" }
        }
    }
}