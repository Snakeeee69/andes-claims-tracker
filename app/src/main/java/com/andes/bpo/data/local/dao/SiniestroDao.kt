package com.andes.bpo.data.local.dao

import androidx.room.*
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import kotlinx.coroutines.flow.Flow
import com.andes.bpo.data.local.entity.UsuarioEntity

@Dao
interface SiniestroDao {

    @Query("SELECT * FROM siniestros WHERE idAsegurado = :idUsuario ORDER BY idSiniestro DESC")
    fun getSiniestrosPorUsuario(idUsuario: Int): Flow<List<SiniestroEntity>>

    @Query("SELECT * FROM siniestros WHERE idSiniestro = :idSiniestro LIMIT 1")
    fun getSiniestroPorId(idSiniestro: Int): Flow<SiniestroEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestro(siniestro: SiniestroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestrosIniciales(siniestros: List<SiniestroEntity>)

    @Query("SELECT * FROM gestiones WHERE idSiniestro = :idSiniestro AND visibleAsegurado = 1 ORDER BY idGestion DESC")
    fun getGestionesVisibles(idSiniestro: Int): Flow<List<GestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGestion(gestion: GestionEntity): Long

    @Query("SELECT * FROM documentos WHERE idSiniestro = :idSiniestro ORDER BY idDocumento ASC")
    fun getDocumentosPorSiniestro(idSiniestro: Int): Flow<List<DocumentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumento(documento: DocumentoEntity): Long

    @Query("UPDATE documentos SET estado = 'Entregado', nombreArchivo = :nombreArchivo, fechaRecepcion = :fechaActual WHERE idDocumento = :idDocumento")
    suspend fun marcarDocumentoEntregado(idDocumento: Int, nombreArchivo: String, fechaActual: String): Int

    @Query("SELECT * FROM avisos ORDER BY idAviso DESC")
    fun getAvisos(): Flow<List<AvisoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAviso(aviso: AvisoEntity): Long


    // Obtener el estado actual de un siniestro
    @Query(
        "SELECT * FROM siniestros WHERE idSiniestro = :id LIMIT 1"
    )
    suspend fun obtenerSiniestroActual(
        id: Int
    ): SiniestroEntity?

    // Contar documentos solicitados que siguen pendientes
    @Query(
        """
        SELECT COUNT(*) FROM documentos
        WHERE idSiniestro = :id
        AND estado = 'Pendiente'
        """
    )
    suspend fun contarDocumentosPendientes(
        id: Int
    ): Int

    // Actualizar el estado del siniestro
    @Query(
        """
        UPDATE siniestros
        SET etapaNombre = :nuevaEtapa,
            proximoPasoTexto = :proximoPaso,
            resultadoCierre = :resultado
        WHERE idSiniestro = :id
        AND etapaNombre = :etapaAnterior
        """
    )
    suspend fun actualizarEtapa(
        id: Int,
        etapaAnterior: String,
        nuevaEtapa: String,
        proximoPaso: String,
        resultado: String?
    ): Int

    @Transaction
    suspend fun avanzarALiquidacion(
        idSiniestro: Int,
        idLiquidador: Int,
        fechaHora: String
    ) {
        val liquidador = obtenerUsuarioParaAsignacion(
            idLiquidador
        ) ?: error("Liquidador no encontrado")

        check(liquidador.rol == "LIQUIDADOR") {
            "No tienes permisos para cambiar etapas"
        }

        val siniestro = obtenerSiniestroActual(
            idSiniestro
        ) ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no está asignado a ti"
        }

        check(siniestro.etapaNombre == "En evaluación") {
            "El siniestro debe estar en evaluación"
        }

        val pendientes = contarDocumentosPendientes(
            idSiniestro
        )

        check(pendientes == 0) {
            "No puedes avanzar: hay documentos pendientes"
        }

        val filas = actualizarEtapa(
            id = idSiniestro,
            etapaAnterior = "En evaluación",
            nuevaEtapa = "En liquidación",
            proximoPaso =
                "Estamos determinando la resolución " +
                        "con tu compañía de seguros.",
            resultado = null
        )

        check(filas == 1) {
            "No se pudo actualizar el estado del siniestro"
        }

        val primerNombre = liquidador.nombre
            .trim()
            .substringBefore(" ")

        val inicialApellido = liquidador.apellido
            .trim()
            .firstOrNull()
            ?.let { "$it." }
            .orEmpty()

        val responsable = "$primerNombre $inicialApellido".trim()

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion =
                    "Tu siniestro pasó a En liquidación. " +
                            "Estamos revisando la resolución del caso.",
                responsable = responsable,
                visibleAsegurado = true
            )
        )
    }


    // Obtener siniestros recibidos y sin liquidador
    @Query(
        """
        SELECT * FROM siniestros
        WHERE etapaNombre = 'Recibido'
        AND idLiquidador IS NULL
        ORDER BY idSiniestro DESC
        """
    )
    fun getSiniestrosSinAsignar(): Flow<List<SiniestroEntity>>


    @Query(
        "SELECT * FROM usuarios WHERE idUsuario = :id LIMIT 1"
    )
    suspend fun obtenerUsuarioParaAsignacion(
        id: Int
    ): UsuarioEntity?

    @Query(
        """
    UPDATE siniestros
    SET idLiquidador = :idLiquidador,
        liquidadorNombre = :nombreLiquidador,
        etapaNombre = 'En evaluación',
        proximoPasoTexto =
            'Un liquidador está revisando tu caso.'
    WHERE idSiniestro = :idSiniestro
      AND idLiquidador IS NULL
      AND etapaNombre = 'Recibido'
    """
    )
    suspend fun actualizarAsignacion(
        idSiniestro: Int,
        idLiquidador: Int,
        nombreLiquidador: String
    ): Int

    @Transaction
    suspend fun asignarLiquidadorConHistorial(
        idSiniestro: Int,
        idLiquidador: Int,
        idAnalista: Int,
        fechaHora: String
    ) {
        val analista = obtenerUsuarioParaAsignacion(idAnalista)
            ?: error("Analista no encontrado")

        check(analista.rol == "ANALISTA") {
            "No tienes permiso para asignar siniestros"
        }

        val liquidador = obtenerUsuarioParaAsignacion(idLiquidador)
            ?: error("Liquidador no encontrado")

        check(liquidador.rol == "LIQUIDADOR") {
            "El usuario seleccionado no es liquidador"
        }

        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        check(
            siniestro.etapaNombre == "Recibido" &&
                    siniestro.idLiquidador == null
        ) {
            "El siniestro ya fue asignado o cambió de etapa"
        }

        val tipo = siniestro.tipoSiniestroNombre.trim()

        val equipoNecesario = when {
            tipo.startsWith("Vehículo", ignoreCase = true) ||
                    tipo.startsWith("Vehiculo", ignoreCase = true) ||
                    tipo.equals("Colisión", ignoreCase = true) ||
                    tipo.equals("Choque", ignoreCase = true) ->
                "VEHICULOS"

            tipo.startsWith("Hogar", ignoreCase = true) ->
                "HOGAR"

            else -> error(
                "No se puede determinar el equipo de este siniestro"
            )
        }

        check(liquidador.equipo == equipoNecesario) {
            "El liquidador pertenece a otro equipo"
        }

        val equipoVisible = if (equipoNecesario == "VEHICULOS") {
            "Vehículos"
        } else {
            "Hogar"
        }

        val nombrePublico = liquidador.nombre
            .trim()
            .substringBefore(" ")

        val inicialApellido = liquidador.apellido
            .trim()
            .firstOrNull()
            ?.let { "$it." }
            .orEmpty()

        val nombreAsignado =
            "$nombrePublico $inicialApellido – Equipo $equipoVisible"

        val filas = actualizarAsignacion(
            idSiniestro = idSiniestro,
            idLiquidador = idLiquidador,
            nombreLiquidador = nombreAsignado
        )

        check(filas == 1) {
            "No se pudo realizar la asignación"
        }

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion =
                    "Tu caso fue asignado a $nombreAsignado. " +
                            "Ahora está en evaluación.",
                responsable = analista.nombre,
                visibleAsegurado = true
            )
        )
    }

    @Query(
        """
    SELECT * FROM siniestros
    WHERE idLiquidador = :idLiquidador
    ORDER BY
        CASE WHEN etapaNombre = 'Cerrado' THEN 1 ELSE 0 END,
        idSiniestro DESC
    """
    )
    fun getSiniestrosAsignados(
        idLiquidador: Int
    ): Flow<List<SiniestroEntity>>

    @Transaction
    suspend fun registrarGestionLiquidador(
        idSiniestro: Int,
        idLiquidador: Int,
        descripcion: String,
        fechaHora: String,
        visibleAsegurado: Boolean
    ) {
        val liquidador = obtenerUsuarioParaAsignacion(
            idLiquidador
        ) ?: error("Liquidador no encontrado")

        check(liquidador.rol == "LIQUIDADOR") {
            "El usuario no tiene permisos de liquidador"
        }

        val siniestro = obtenerSiniestroActual(
            idSiniestro
        ) ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no está asignado a ti"
        }

        check(siniestro.etapaNombre != "Cerrado") {
            "No puedes registrar gestiones en un caso cerrado"
        }

        val texto = descripcion.trim()

        require(texto.length in 5..500) {
            "La descripción debe tener entre 5 y 500 caracteres"
        }

        require(fechaHora.isNotBlank()) {
            "La fecha es obligatoria"
        }

        val primerNombre = liquidador.nombre
            .trim()
            .substringBefore(" ")

        val inicialApellido = liquidador.apellido
            .trim()
            .firstOrNull()
            ?.let { "$it." }
            .orEmpty()

        val responsable = "$primerNombre $inicialApellido".trim()

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = texto,
                responsable = responsable,
                visibleAsegurado = visibleAsegurado
            )
        )
    }
    @Query(
        """
    UPDATE siniestros
    SET etapaNombre = 'Cerrado',
        resultadoCierre = :resultado,
        mensajeCierre = :mensaje,
        proximoPasoTexto =
            'Tu siniestro está cerrado. Revisa el resultado.'
    WHERE idSiniestro = :idSiniestro
      AND idLiquidador = :idLiquidador
      AND etapaNombre = :etapaAnterior
    """
    )
    suspend fun actualizarCierre(
        idSiniestro: Int,
        idLiquidador: Int,
        etapaAnterior: String,
        resultado: String,
        mensaje: String
    ): Int


    @Transaction
    suspend fun cerrarSiniestroConHistorial(
        idSiniestro: Int,
        idLiquidador: Int,
        resultado: String,
        mensaje: String,
        fechaHora: String
    ) {
        val liquidador = obtenerUsuarioParaAsignacion(
            idLiquidador
        ) ?: error("Liquidador no encontrado")

        check(liquidador.rol == "LIQUIDADOR") {
            "No tienes permisos para cerrar siniestros"
        }

        val siniestro = obtenerSiniestroActual(
            idSiniestro
        ) ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no está asignado a ti"
        }

        val cierrePermitido = when (siniestro.etapaNombre) {
            "En evaluación" ->
                resultado == "Rechazado"

            "En liquidación" ->
                resultado == "Aprobado" ||
                        resultado == "Rechazado"

            else -> false
        }

        check(cierrePermitido) {
            "El resultado no está permitido en esta etapa"
        }

        val mensajeLimpio = mensaje.trim()

        require(mensajeLimpio.length in 10..500) {
            "El mensaje debe tener entre 10 y 500 caracteres"
        }

        require(fechaHora.isNotBlank()) {
            "La fecha de cierre es obligatoria"
        }

        val filas = actualizarCierre(
            idSiniestro = idSiniestro,
            idLiquidador = idLiquidador,
            etapaAnterior = siniestro.etapaNombre,
            resultado = resultado,
            mensaje = mensajeLimpio
        )

        check(filas == 1) {
            "No se pudo cerrar el siniestro"
        }

        val primerNombre = liquidador.nombre
            .trim()
            .substringBefore(" ")

        val inicialApellido = liquidador.apellido
            .trim()
            .firstOrNull()
            ?.let { "$it." }
            .orEmpty()

        val responsable =
            "$primerNombre $inicialApellido".trim()

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion =
                    "Tu siniestro fue cerrado. " +
                            "Resultado: $resultado. " +
                            mensajeLimpio,
                responsable = responsable,
                visibleAsegurado = true
            )
        )
    }

    @Query(
        """
    SELECT * FROM documentos
    WHERE idSiniestro = :idSiniestro
    AND rutaUri IS NOT NULL
    ORDER BY idDocumento DESC
    """
    )
    fun getEvidenciasPorSiniestro(
        idSiniestro: Int
    ): Flow<List<DocumentoEntity>>

    @Query(
        """
    SELECT COUNT(*) FROM documentos
    WHERE idSiniestro = :idSiniestro
    AND rutaUri IS NOT NULL
    """
    )
    suspend fun contarEvidencias(
        idSiniestro: Int
    ): Int

    @Query("SELECT * FROM documentos WHERE idDocumento = :id LIMIT 1")
    suspend fun getDocumentoPorId(id: Int): DocumentoEntity?

    @Query(
        """
    DELETE FROM documentos
    WHERE idDocumento = :idDocumento
      AND idSiniestro = :idSiniestro
      AND estado = 'Borrador'
    """
    )
    suspend fun eliminarDocumentoBorrador(
        idDocumento: Int,
        idSiniestro: Int
    ): Int

    @Query(
        """
    UPDATE documentos
    SET estado = 'Entregado', fechaRecepcion = :fecha
    WHERE idDocumento = :idDocumento
      AND idSiniestro = :idSiniestro
      AND estado = 'Borrador'
    """
    )
    suspend fun marcarBorradorEnviado(
        idDocumento: Int,
        idSiniestro: Int,
        fecha: String
    ): Int

    @Transaction
    suspend fun enviarEvidenciaConHistorial(
        idDocumento: Int,
        idSiniestro: Int,
        idAsegurado: Int,
        fechaHora: String
    ) {
        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        check(siniestro.idAsegurado == idAsegurado) {
            "Este siniestro no te pertenece"
        }
        check(siniestro.etapaNombre != "Cerrado") {
            "No puedes enviar documentos de un caso cerrado"
        }

        val borrador = getDocumentoPorId(idDocumento)
            ?: error("Documento no encontrado")

        check(borrador.idSiniestro == idSiniestro && borrador.estado == "Borrador") {
            "El documento no está disponible para enviar"
        }
        check(!borrador.rutaUri.isNullOrBlank()) {
            "El borrador no tiene un archivo asociado"
        }

        // Si hay una solicitud pendiente del mismo tipo, completamos ESA FILA.
        // Así conservamos su fecha y comentario, en vez de dejarla pendiente
        // y crear otro registro separado que no responde a la solicitud.
        val solicitud = buscarSolicitudPendiente(idSiniestro, borrador.nombreTipoDocumento)
        if (solicitud != null) {
            check(
                completarSolicitudConArchivo(
                    idSolicitud = solicitud.idDocumento,
                    idSiniestro = idSiniestro,
                    nombreArchivo = borrador.nombreArchivo,
                    rutaUri = borrador.rutaUri!!,
                    fecha = fechaHora
                ) == 1
            ) { "No se pudo completar la solicitud" }

            // Eliminamos únicamente la fila del borrador, no el archivo físico.
            check(eliminarDocumentoBorrador(idDocumento, idSiniestro) == 1) {
                "No se pudo finalizar el envío"
            }
        } else {
            // Envío de evidencia espontánea, sin una solicitud del liquidador.
            check(marcarBorradorEnviado(idDocumento, idSiniestro, fechaHora) == 1) {
                "No se pudo marcar el documento como enviado"
            }
        }

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = if (solicitud != null) {
                    "Entregaste el documento solicitado: ${borrador.nombreTipoDocumento}."
                } else {
                    "Recibimos el documento: ${borrador.nombreTipoDocumento}."
                },
                responsable = "Asegurado",
                visibleAsegurado = true
            )
        )
    }

    @Query(
        """
    SELECT COUNT(*) FROM documentos
    WHERE idSiniestro = :idSiniestro
    AND nombreTipoDocumento = :tipoDocumento
    AND estado = 'Pendiente'
    """
    )
    suspend fun contarSolicitudesDelTipo(
        idSiniestro: Int,
        tipoDocumento: String
    ): Int


    @Transaction
    suspend fun solicitarDocumentoConHistorial(
        idSiniestro: Int,
        idLiquidador: Int,
        tipoDocumento: String,
        comentario: String,
        fechaHora: String
    ) {
        val liquidador = obtenerUsuarioParaAsignacion(
            idLiquidador
        ) ?: error("Liquidador no encontrado")

        check(liquidador.rol == "LIQUIDADOR") {
            "No tienes permisos para solicitar documentos"
        }

        val siniestro = obtenerSiniestroActual(
            idSiniestro
        ) ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no está asignado a ti"
        }

        check(siniestro.etapaNombre == "En evaluación") {
            "Solo puedes solicitar documentos durante la evaluación"
        }

        val tipo = tipoDocumento.trim()
        val comentarioLimpio = comentario.trim()

        require(tipo.isNotBlank()) {
            "Selecciona el tipo de documento"
        }

        require(comentarioLimpio.length in 5..300) {
            "El comentario debe tener entre 5 y 300 caracteres"
        }

        require(fechaHora.isNotBlank()) {
            "La fecha de solicitud es obligatoria"
        }

        check(
            contarSolicitudesDelTipo(idSiniestro, tipo) == 0
        ) {
            "Ya existe una solicitud pendiente de este tipo"
        }

        // Registrar el documento solicitado
        insertDocumento(
            DocumentoEntity(
                idSiniestro = idSiniestro,
                nombreTipoDocumento = tipo,
                comentario = comentarioLimpio,
                fechaSolicitud = fechaHora,
                estado = "Pendiente",
                nombreArchivo = null,
                rutaUri = null,
                fechaRecepcion = null
            )
        )

        val primerNombre = liquidador.nombre
            .trim()
            .substringBefore(" ")

        val inicialApellido = liquidador.apellido
            .trim()
            .firstOrNull()
            ?.let { "$it." }
            .orEmpty()

        val responsable =
            "$primerNombre $inicialApellido".trim()

        // Registrar la solicitud en el historial
        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion =
                    "Te solicitamos el documento: $tipo. " +
                            comentarioLimpio,
                responsable = responsable,
                visibleAsegurado = true
            )
        )
    }
    @Query(
        """
    SELECT * FROM documentos
    WHERE idSiniestro = :idSiniestro
      AND fechaSolicitud != ''
    ORDER BY CASE WHEN estado = 'Pendiente' THEN 0 ELSE 1 END,
             idDocumento DESC
    """
    )
    fun getSolicitudesPorSiniestro(
        idSiniestro: Int
    ): Flow<List<DocumentoEntity>>

    @Query(
        """
    SELECT * FROM documentos
    WHERE idSiniestro = :idSiniestro
      AND nombreTipoDocumento = :tipo
      AND estado = 'Pendiente'
      AND rutaUri IS NULL
    LIMIT 1
    """
    )
    suspend fun buscarSolicitudPendiente(
        idSiniestro: Int,
        tipo: String
    ): DocumentoEntity?

    @Query(
        """
    UPDATE documentos
    SET estado = 'Entregado',
        nombreArchivo = :nombreArchivo,
        rutaUri = :rutaUri,
        fechaRecepcion = :fecha
    WHERE idDocumento = :idSolicitud
      AND idSiniestro = :idSiniestro
      AND estado = 'Pendiente'
      AND rutaUri IS NULL
    """
    )
    suspend fun completarSolicitudConArchivo(
        idSolicitud: Int,
        idSiniestro: Int,
        nombreArchivo: String?,
        rutaUri: String,
        fecha: String
    ): Int

    @Query(
        """
    SELECT * FROM documentos
    WHERE idSiniestro = :idSiniestro
      AND estado IN ('Pendiente', 'Entregado')
    ORDER BY CASE WHEN estado = 'Pendiente' THEN 0 ELSE 1 END,
             idDocumento DESC
    """
    )
    fun getDocumentosParaLiquidador(
        idSiniestro: Int
    ): Flow<List<DocumentoEntity>>
}