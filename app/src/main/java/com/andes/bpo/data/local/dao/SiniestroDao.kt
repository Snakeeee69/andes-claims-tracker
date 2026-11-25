package com.andes.bpo.data.local.dao

import androidx.room.*
import com.andes.bpo.data.local.entity.AvisoEntity
import com.andes.bpo.data.local.entity.DocumentoEntity
import com.andes.bpo.data.local.entity.GestionEntity
import com.andes.bpo.data.local.entity.SiniestroEntity
import com.andes.bpo.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiniestroDao {

    @Query("SELECT * FROM siniestros WHERE idAsegurado = :idUsuario ORDER BY idSiniestro DESC")
    fun getSiniestrosPorUsuario(idUsuario: Int): Flow<List<SiniestroEntity>>

    @Query("SELECT * FROM siniestros ORDER BY idSiniestro DESC")
    fun getTodosLosSiniestros(): Flow<List<SiniestroEntity>>

    @Query("SELECT * FROM siniestros WHERE idSiniestro = :idSiniestro LIMIT 1")
    fun getSiniestroPorId(idSiniestro: Int): Flow<SiniestroEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestro(siniestro: SiniestroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiniestrosIniciales(siniestros: List<SiniestroEntity>)

    @Update
    suspend fun actualizarSiniestro(siniestro: SiniestroEntity)

    @Query("SELECT * FROM gestiones WHERE idSiniestro = :idSiniestro AND visibleAsegurado = 1 ORDER BY idGestion DESC")
    fun getGestionesVisibles(idSiniestro: Int): Flow<List<GestionEntity>>

    @Query("SELECT * FROM gestiones WHERE idSiniestro = :idSiniestro ORDER BY idGestion DESC")
    fun getTodasLasGestiones(idSiniestro: Int): Flow<List<GestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGestion(gestion: GestionEntity): Long

    @Query("SELECT * FROM documentos WHERE idSiniestro = :idSiniestro ORDER BY idDocumento ASC")
    fun getDocumentosPorSiniestro(idSiniestro: Int): Flow<List<DocumentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumento(documento: DocumentoEntity): Long

    @Query("DELETE FROM documentos WHERE idDocumento = :idDocumento")
    suspend fun eliminarDocumentoPorId(idDocumento: Int): Int

    @Query("UPDATE documentos SET estado = 'Entregado', nombreArchivo = :nombreArchivo, fechaRecepcion = :fechaActual WHERE idDocumento = :idDocumento")
    suspend fun marcarDocumentoEntregado(idDocumento: Int, nombreArchivo: String, fechaActual: String): Int

    @Query("SELECT * FROM avisos ORDER BY idAviso DESC")
    fun getAvisos(): Flow<List<AvisoEntity>>

    @Query("SELECT * FROM avisos WHERE idUsuario = :idUsuario ORDER BY idAviso DESC")
    fun getAvisosPorUsuario(idUsuario: Int): Flow<List<AvisoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAviso(aviso: AvisoEntity): Long

    @Query("UPDATE avisos SET leido = 1, estadoAviso = :nuevoEstado WHERE idAviso = :idAviso")
    suspend fun actualizarEstadoAviso(idAviso: Int, nuevoEstado: String)

    @Query("SELECT * FROM usuarios WHERE rol IN ('ANALISTA', 'ADMINISTRADOR')")
    suspend fun obtenerAnalistasYAdmins(): List<UsuarioEntity>

    @Query("SELECT * FROM siniestros WHERE idSiniestro = :id LIMIT 1")
    suspend fun obtenerSiniestroActual(id: Int): SiniestroEntity?

    @Query(
        """
        SELECT COUNT(*) FROM documentos
        WHERE idSiniestro = :id
        AND estado = 'Pendiente'
        """
    )
    suspend fun contarDocumentosPendientes(id: Int): Int

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
        val liquidador = obtenerUsuarioParaAsignacion(idLiquidador)
            ?: error("Liquidador no encontrado")

        check(liquidador.rol.startsWith("LIQUIDADOR")) {
            "No tienes permisos para cambiar etapas"
        }

        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no estÃ¡ asignado a ti"
        }

        check(siniestro.etapaNombre == "En evaluaciÃ³n") {
            "El siniestro debe estar en evaluaciÃ³n"
        }

        val pendientes = contarDocumentosPendientes(idSiniestro)

        check(pendientes == 0) {
            "No puedes avanzar: hay documentos pendientes"
        }

        val filas = actualizarEtapa(
            id = idSiniestro,
            etapaAnterior = "En evaluaciÃ³n",
            nuevaEtapa = "En liquidaciÃ³n",
            proximoPaso = "Estamos determinando la resoluciÃ³n con tu compaÃ±Ã­a de seguros.",
            resultado = null
        )

        check(filas == 1) {
            "No se pudo actualizar el estado del siniestro"
        }

        val primerNombre = liquidador.nombre.trim().substringBefore(" ")
        val inicialApellido = liquidador.apellido.trim().firstOrNull()?.let { "$it." }.orEmpty()
        val responsable = "$primerNombre $inicialApellido".trim()

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "Tu siniestro pasÃ³ a En liquidaciÃ³n. Estamos revisando la resoluciÃ³n del caso.",
                responsable = responsable,
                visibleAsegurado = true
            )
        )

        insertAviso(
            AvisoEntity(
                fechaHora = fechaHora,
                mensaje = "Tu siniestro ${siniestro.numero} avanzÃ³ a etapa En liquidaciÃ³n.",
                leido = false,
                idUsuario = siniestro.idAsegurado,
                requiereAccion = false,
                estadoAviso = "PENDIENTE"
            )
        )
    }

    @Query(
        """
        SELECT * FROM siniestros
        WHERE etapaNombre = 'Recibido'
        AND idLiquidador IS NULL
        ORDER BY idSiniestro DESC
        """
    )
    fun getSiniestrosSinAsignar(): Flow<List<SiniestroEntity>>

    @Query("SELECT * FROM usuarios WHERE idUsuario = :id LIMIT 1")
    suspend fun obtenerUsuarioParaAsignacion(id: Int): UsuarioEntity?

    @Query(
        """
        UPDATE siniestros
        SET idLiquidador = :idLiquidador,
            liquidadorNombre = :nombreLiquidador,
            etapaNombre = 'En evaluaciÃ³n',
            aceptadoPorLiquidador = NULL,
            motivoRechazoLiquidador = NULL,
            proximoPasoTexto = 'Un liquidador estÃ¡ revisando tu caso.'
        WHERE idSiniestro = :idSiniestro
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

        check(analista.rol == "ANALISTA" || analista.rol == "ADMINISTRADOR") {
            "No tienes permiso para asignar siniestros"
        }

        val liquidador = obtenerUsuarioParaAsignacion(idLiquidador)
            ?: error("Liquidador no encontrado")

        check(liquidador.rol.startsWith("LIQUIDADOR")) {
            "El usuario seleccionado no es liquidador"
        }

        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        val tipo = siniestro.tipoSiniestroNombre.trim()

        val equipoNecesario = when {
            siniestro.idArea == 1 ||
                    tipo.startsWith("VehÃ­culo", ignoreCase = true) ||
                    tipo.startsWith("Vehiculo", ignoreCase = true) ||
                    tipo.equals("ColisiÃ³n", ignoreCase = true) ||
                    tipo.equals("Choque", ignoreCase = true) -> "VEHICULOS"
            siniestro.idArea == 2 ||
                    tipo.startsWith("Hogar", ignoreCase = true) ||
                    tipo.contains("Incendio", ignoreCase = true) -> "HOGAR"
            siniestro.idArea == 3 ||
                    tipo.startsWith("Salud", ignoreCase = true) ||
                    tipo.startsWith("Vida", ignoreCase = true) -> "SALUD"
            else -> error("No se puede determinar el equipo de este siniestro")
        }

        check(liquidador.equipo == equipoNecesario) {
            "El liquidador pertenece a otro equipo"
        }

        val equipoVisible = when (equipoNecesario) {
            "VEHICULOS" -> "VehÃ­culos"
            "HOGAR" -> "Hogar"
            else -> "Salud"
        }

        val nombrePublico = liquidador.nombre.trim().substringBefore(" ")
        val inicialApellido = liquidador.apellido.trim().firstOrNull()?.let { "$it." }.orEmpty()
        val nombreAsignado = "$nombrePublico $inicialApellido â€“ Equipo $equipoVisible"

        val filas = actualizarAsignacion(
            idSiniestro = idSiniestro,
            idLiquidador = idLiquidador,
            nombreLiquidador = nombreAsignado
        )

        check(filas == 1) {
            "No se pudo realizar la asignaciÃ³n"
        }

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "Tu caso fue asignado a $nombreAsignado. Ahora estÃ¡ en evaluaciÃ³n.",
                responsable = analista.nombre,
                visibleAsegurado = true
            )
        )

        insertAviso(
            AvisoEntity(
                fechaHora = fechaHora,
                mensaje = "Nuevo caso asignado: ${siniestro.numero} (${siniestro.tipoSiniestroNombre}). Tienes 2 dÃ­as hÃ¡biles para aceptar o rechazar.",
                leido = false,
                idUsuario = idLiquidador,
                requiereAccion = true,
                estadoAviso = "PENDIENTE"
            )
        )

        insertAviso(
            AvisoEntity(
                fechaHora = fechaHora,
                mensaje = "Tu siniestro ${siniestro.numero} fue asignado a $nombreAsignado.",
                leido = false,
                idUsuario = siniestro.idAsegurado,
                requiereAccion = false,
                estadoAviso = "PENDIENTE"
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
    fun getSiniestrosAsignados(idLiquidador: Int): Flow<List<SiniestroEntity>>

    @Transaction
    suspend fun registrarGestionLiquidador(
        idSiniestro: Int,
        idLiquidador: Int,
        descripcion: String,
        fechaHora: String,
        visibleAsegurado: Boolean
    ) {
        val liquidador = obtenerUsuarioParaAsignacion(idLiquidador)
            ?: error("Liquidador no encontrado")

        check(liquidador.rol.startsWith("LIQUIDADOR")) {
            "El usuario no tiene permisos de liquidador"
        }

        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no estÃ¡ asignado a ti"
        }

        check(siniestro.etapaNombre != "Cerrado") {
            "No puedes registrar gestiones en un caso cerrado"
        }

        val texto = descripcion.trim()

        require(texto.length in 5..500) {
            "La descripciÃ³n debe tener entre 5 y 500 caracteres"
        }

        require(fechaHora.isNotBlank()) {
            "La fecha es obligatoria"
        }

        val primerNombre = liquidador.nombre.trim().substringBefore(" ")
        val inicialApellido = liquidador.apellido.trim().firstOrNull()?.let { "$it." }.orEmpty()
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
            proximoPasoTexto = 'Tu siniestro estÃ¡ cerrado. Revisa el resultado.'
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
        val liquidador = obtenerUsuarioParaAsignacion(idLiquidador)
            ?: error("Liquidador no encontrado")

        check(liquidador.rol.startsWith("LIQUIDADOR")) {
            "No tienes permisos para cerrar siniestros"
        }

        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no estÃ¡ asignado a ti"
        }

        val cierrePermitido = when (siniestro.etapaNombre) {
            "En evaluaciÃ³n" -> resultado == "Rechazado"
            "En liquidaciÃ³n" -> resultado == "Aprobado" || resultado == "Rechazado"
            else -> false
        }

        check(cierrePermitido) {
            "El resultado no estÃ¡ permitido en esta etapa"
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

        val primerNombre = liquidador.nombre.trim().substringBefore(" ")
        val inicialApellido = liquidador.apellido.trim().firstOrNull()?.let { "$it." }.orEmpty()
        val responsable = "$primerNombre $inicialApellido".trim()

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "Tu siniestro fue cerrado. Resultado: $resultado. $mensajeLimpio",
                responsable = responsable,
                visibleAsegurado = true
            )
        )

        insertAviso(
            AvisoEntity(
                fechaHora = fechaHora,
                mensaje = "Tu siniestro ${siniestro.numero} ha sido cerrado con resultado: $resultado.",
                leido = false,
                idUsuario = siniestro.idAsegurado,
                requiereAccion = false,
                estadoAviso = "PENDIENTE"
            )
        )

        obtenerAnalistasYAdmins().forEach { analista ->
            insertAviso(
                AvisoEntity(
                    fechaHora = fechaHora,
                    mensaje = "Siniestro ${siniestro.numero} cerrado ($resultado) por ${liquidador.nombre} ${liquidador.apellido}.",
                    leido = false,
                    idUsuario = analista.idUsuario,
                    requiereAccion = false,
                    estadoAviso = "PENDIENTE"
                )
            )
        }
    }

    @Query(
        """
        SELECT * FROM documentos
        WHERE idSiniestro = :idSiniestro
        AND rutaUri IS NOT NULL
        ORDER BY idDocumento DESC
        """
    )
    fun getEvidenciasPorSiniestro(idSiniestro: Int): Flow<List<DocumentoEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM documentos
        WHERE idSiniestro = :idSiniestro
        AND rutaUri IS NOT NULL
        """
    )
    suspend fun contarEvidencias(idSiniestro: Int): Int

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
    suspend fun eliminarDocumentoBorrador(idDocumento: Int, idSiniestro: Int): Int

    @Query(
        """
        UPDATE documentos
        SET estado = 'Entregado', fechaRecepcion = :fecha
        WHERE idDocumento = :idDocumento
          AND idSiniestro = :idSiniestro
          AND estado = 'Borrador'
        """
    )
    suspend fun marcarBorradorEnviado(idDocumento: Int, idSiniestro: Int, fecha: String): Int

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
            "El documento no estÃ¡ disponible para enviar"
        }
        check(!borrador.rutaUri.isNullOrBlank()) {
            "El borrador no tiene un archivo asociado"
        }

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

            check(eliminarDocumentoBorrador(idDocumento, idSiniestro) == 1) {
                "No se pudo finalizar el envÃ­o"
            }
        } else {
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

        siniestro.idLiquidador?.let { idLiq ->
            insertAviso(
                AvisoEntity(
                    fechaHora = fechaHora,
                    mensaje = "Nueva evidencia adjunta en ${siniestro.numero}: ${borrador.nombreTipoDocumento}.",
                    leido = false,
                    idUsuario = idLiq,
                    requiereAccion = true,
                    estadoAviso = "PENDIENTE"
                )
            )
        }

        obtenerAnalistasYAdmins().forEach { analista ->
            insertAviso(
                AvisoEntity(
                    fechaHora = fechaHora,
                    mensaje = "El asegurado subiÃ³ evidencia (${borrador.nombreTipoDocumento}) en el caso ${siniestro.numero}.",
                    leido = false,
                    idUsuario = analista.idUsuario,
                    requiereAccion = false,
                    estadoAviso = "PENDIENTE"
                )
            )
        }
    }

    @Query(
        """
        SELECT COUNT(*) FROM documentos
        WHERE idSiniestro = :idSiniestro
        AND nombreTipoDocumento = :tipoDocumento
        AND estado = 'Pendiente'
        """
    )
    suspend fun contarSolicitudesDelTipo(idSiniestro: Int, tipoDocumento: String): Int

    @Transaction
    suspend fun solicitarDocumentoConHistorial(
        idSiniestro: Int,
        idLiquidador: Int,
        tipoDocumento: String,
        comentario: String,
        fechaHora: String
    ) {
        val liquidador = obtenerUsuarioParaAsignacion(idLiquidador)
            ?: error("Liquidador no encontrado")

        check(liquidador.rol.startsWith("LIQUIDADOR")) {
            "No tienes permisos para solicitar documentos"
        }

        val siniestro = obtenerSiniestroActual(idSiniestro)
            ?: error("Siniestro no encontrado")

        check(siniestro.idLiquidador == idLiquidador) {
            "Este siniestro no estÃ¡ asignado a ti"
        }

        check(siniestro.etapaNombre != "Cerrado") {
            "No puedes solicitar documentos en un siniestro cerrado"
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

        check(contarSolicitudesDelTipo(idSiniestro, tipo) == 0) {
            "Ya existe una solicitud pendiente de este tipo"
        }

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

        val primerNombre = liquidador.nombre.trim().substringBefore(" ")
        val inicialApellido = liquidador.apellido.trim().firstOrNull()?.let { "$it." }.orEmpty()
        val responsable = "$primerNombre $inicialApellido".trim()

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "Te solicitamos el documento: $tipo. $comentarioLimpio",
                responsable = responsable,
                visibleAsegurado = true
            )
        )

        insertAviso(
            AvisoEntity(
                fechaHora = fechaHora,
                mensaje = "Documento solicitado en ${siniestro.numero}: $tipo ($comentarioLimpio).",
                leido = false,
                idUsuario = siniestro.idAsegurado,
                requiereAccion = true,
                estadoAviso = "PENDIENTE"
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
    fun getSolicitudesPorSiniestro(idSiniestro: Int): Flow<List<DocumentoEntity>>

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
    suspend fun buscarSolicitudPendiente(idSiniestro: Int, tipo: String): DocumentoEntity?

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
    fun getDocumentosParaLiquidador(idSiniestro: Int): Flow<List<DocumentoEntity>>

    @Transaction
    suspend fun aplicarProrrogaAnalista(
        idSiniestro: Int,
        idAnalista: Int,
        diasProrroga: Int,
        motivo: String,
        fechaHora: String
    ) {
        val analista = obtenerUsuarioParaAsignacion(idAnalista) ?: error("Analista no encontrado")
        check(analista.rol == "ANALISTA" || analista.rol == "ADMINISTRADOR") { "Sin permisos" }
        require(diasProrroga == 90 || diasProrroga == 180) { "La prÃ³rroga legal debe ser de 90 o 180 dÃ­as" }

        val siniestro = obtenerSiniestroActual(idSiniestro) ?: error("Siniestro no encontrado")
        actualizarSiniestro(siniestro.copy(diasProrroga = diasProrroga))

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "PrÃ³rroga fundada aplicada por $diasProrroga dÃ­as (D.S. NÂ° 1.055). Motivo: $motivo",
                responsable = "${analista.nombre} ${analista.apellido}",
                visibleAsegurado = true
            )
        )

        siniestro.idLiquidador?.let { idLiq ->
            insertAviso(
                AvisoEntity(
                    fechaHora = fechaHora,
                    mensaje = "PrÃ³rroga de $diasProrroga dÃ­as aplicada al caso ${siniestro.numero}.",
                    leido = false,
                    idUsuario = idLiq,
                    requiereAccion = false,
                    estadoAviso = "PENDIENTE"
                )
            )
        }
    }

    @Transaction
    suspend fun aceptarCasoLiquidador(
        idSiniestro: Int,
        idLiquidador: Int,
        fechaHora: String
    ) {
        val liq = obtenerUsuarioParaAsignacion(idLiquidador) ?: error("Liquidador no encontrado")
        val siniestro = obtenerSiniestroActual(idSiniestro) ?: error("Siniestro no encontrado")
        check(siniestro.idLiquidador == idLiquidador) { "Caso no asignado a este liquidador" }

        actualizarSiniestro(
            siniestro.copy(
                etapaNombre = if (siniestro.etapaNombre == "Recibido") "En evaluaciÃ³n" else siniestro.etapaNombre,
                aceptadoPorLiquidador = true,
                proximoPasoTexto = "El liquidador aceptÃ³ el caso y estÃ¡ revisando antecedentes."
            )
        )

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "Caso aceptado formalmente por el liquidador ${liq.nombre} ${liq.apellido}.",
                responsable = "${liq.nombre} ${liq.apellido}",
                visibleAsegurado = true
            )
        )

        obtenerAnalistasYAdmins().forEach { analista ->
            insertAviso(
                AvisoEntity(
                    fechaHora = fechaHora,
                    mensaje = "${liq.nombre} ${liq.apellido} aceptÃ³ el caso ${siniestro.numero}.",
                    leido = false,
                    idUsuario = analista.idUsuario,
                    requiereAccion = false,
                    estadoAviso = "PENDIENTE"
                )
            )
        }
    }

    @Transaction
    suspend fun rechazarYDerivarALiquidador2(
        idSiniestro: Int,
        idLiquidador1: Int,
        idLiquidador2: Int?,
        nombreLiquidador2: String?,
        motivoRechazo: String,
        fechaHora: String
    ) {
        val liq1 = obtenerUsuarioParaAsignacion(idLiquidador1) ?: error("Liquidador origen no encontrado")
        val siniestro = obtenerSiniestroActual(idSiniestro) ?: error("Siniestro no encontrado")
        check(siniestro.idLiquidador == idLiquidador1) { "Este caso no estÃ¡ asignado a ti" }
        require(motivoRechazo.trim().length >= 10) { "Debes indicar un motivo fundado de al menos 10 caracteres" }

        actualizarSiniestro(
            siniestro.copy(
                idLiquidador = idLiquidador2,
                liquidadorNombre = if (idLiquidador2 != null) "$nombreLiquidador2 (Respaldo)" else "Por reasignar",
                etapaNombre = if (idLiquidador2 != null) "En evaluaciÃ³n" else "Recibido",
                aceptadoPorLiquidador = null,
                motivoRechazoLiquidador = motivoRechazo.trim(),
                proximoPasoTexto = if (idLiquidador2 != null) {
                    "Caso reasignado a liquidador de respaldo."
                } else {
                    "Caso rechazado por liquidador; pendiente de reasignaciÃ³n."
                }
            )
        )

        insertGestion(
            GestionEntity(
                idSiniestro = idSiniestro,
                fechaHora = fechaHora,
                descripcion = "Liquidador ${liq1.nombre} ${liq1.apellido} rechazÃ³ la asignaciÃ³n (${motivoRechazo.trim()}).",
                responsable = "${liq1.nombre} ${liq1.apellido}",
                visibleAsegurado = true
            )
        )

        obtenerAnalistasYAdmins().forEach { analista ->
            insertAviso(
                AvisoEntity(
                    fechaHora = fechaHora,
                    mensaje = "URGENTE: ${liq1.nombre} ${liq1.apellido} rechazÃ³ ${siniestro.numero} (${motivoRechazo.trim()}). Requiere revisiÃ³n.",
                    leido = false,
                    idUsuario = analista.idUsuario,
                    requiereAccion = true,
                    estadoAviso = "PENDIENTE"
                )
            )
        }
    }
}
