package org.example.service

import config.Config
import org.apache.ibatis.jdbc.ScriptRunner
import org.lighthousegames.logging.logging
import java.io.PrintWriter
import java.io.Reader
import java.sql.Connection
import java.sql.DriverManager

private val logger = logging()

/**
 * Inicializador de la base de datos H2
 */
object DataBaseInitializer {

    /**
     * Inicializamos la base de datos
     */
    fun initialize() {
        // Iniciamos la base de datos
        initConexion()
        if (Config.databaseInitTables) {
            initTablas()
        }
        if (Config.databaseInitData) {
            initData()
        }
    }

    /**
     * Inicializamos los datos de la base de datos si está configurado
     */
    private fun initData() {
        logger.debug { "Iniciando carga de datos en H2" }
        try {
            val data = ClassLoader.getSystemResourceAsStream("data.sql")?.bufferedReader()
            data?.let {
                scriptRunner(it, true)
                logger.debug { "Datos cargados correctamente" }
            } ?: logger.error { "No se pudo cargar el archivo de datos" }
        } catch (e: Exception) {
            logger.error { "Error al cargar los datos: ${e.message}" }
        }
    }

    /**
     * Inicializamos las tablas de la base de datos si está configurado
     */
    private fun initTablas() {
        logger.debug { "Creando tablas en H2" }
        try {
            val tablas = ClassLoader.getSystemResourceAsStream("tables.sql")?.bufferedReader()
            tablas?.let {
                scriptRunner(it, true)
                logger.debug { "Tablas creadas correctamente" }
            } ?: logger.error { "No se pudo cargar el archivo de tablas" }
        } catch (e: Exception) {
            logger.error { "Error al crear las tablas: ${e.message}" }
        }
    }


    /**
     * Inicializamos la conexión con la base de datos H2
     */
    private fun initConexion() {
        // Inicializamos la base de datos
        logger.debug { "Iniciando conexión con la base de datos" }
        try {
            DriverManager.getConnection(Config.databaseUrl)
        } catch (e: Exception) {
            logger.error { "Error al conectar con la base de datos: ${e.message}" }
        }
        logger.debug { "Conexión con la base de datos inicializada" }
    }

    /**
     * Función para ejecutar un script SQL en la base de datos
     */
    private fun scriptRunner(reader: Reader, logWriter: Boolean = false) {
        logger.debug { "Ejecutando script SQL con log: $logWriter" }
        val connection = DriverManager.getConnection(Config.databaseUrl)
        val sr = ScriptRunner(connection)
        sr.setLogWriter(if (logWriter) PrintWriter(System.out) else null)
        sr.runScript(reader)
    }
}

/**
 * Clase para gestionar la conexión con la base de datos H2
 */
object DataBaseManager : AutoCloseable {
    var connection: Connection? = null
        private set

    init {
        DataBaseInitializer.initialize()
    }

    private fun initConexion() {
        // Inicializamos la base de datos
        logger.debug { "Iniciando conexión con la base de datos" }
        if (connection == null || connection!!.isClosed) {
            connection = DriverManager.getConnection(Config.databaseUrl)
        }
        logger.debug { "Conexión con la base de datos inicializada" }
    }

    /**
     * Cerramos la conexión con la base de datos
     */
    override fun close() {
        logger.debug { "Cerrando conexión con la base de datos" }
        if (!connection!!.isClosed) {
            connection!!.close()
        }
        logger.debug { "Conexión con la base de datos cerrada" }
    }

    /**
     * Función para usar la base de datos y cerrarla al finalizar la operación
     */

    fun <T> use(block: (DataBaseManager) -> T) {
        try {
            initConexion()
            block(this)
        } catch (e: Exception) {
            logger.error { "Error en la base de datos: ${e.message}" }
            throw e
        } finally {
            close()
        }
    }
}
