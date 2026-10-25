# Andes BPO - Seguimiento Móvil de Siniestros (MVP)

Aplicación móvil Android desarrollada como Producto Mínimo Viable (MVP) para Servicios Corporativos Andes SpA. Permite a las personas aseguradas consultar en tiempo real el estado, avance y requerimientos de sus siniestros reportados, reduciendo la necesidad de contacto por canales tradicionales.

PROYECTO ACADÉMICO:
* Asignatura: DSY1105 - Desarrollo de Aplicaciones Móviles
* Institución: Duoc UC (Sede Plaza Norte / CITT)
* Entrega: Evaluación Parcial 2

---

## Tecnologías y Arquitectura

* Lenguaje: Kotlin
* Interfaz de Usuario: Jetpack Compose con Material Design 3
* Arquitectura: MVVM (Model-View-ViewModel) + Clean Architecture (Repository Pattern)
* Persistencia Local: Room Database (SQLite) con precarga reactiva mediante StateFlow y Corrutinas
* Navegación: Jetpack Navigation Compose
* Recursos Nativos: Integración con la cámara nativa (FileProvider), galería de imágenes y gestor de archivos para evidencia PDF.

---

## Funcionalidades Principales

1. Autenticación e Identificación: Validación de RUT chileno con algoritmo de dígito verificador y visual transformation.
2. Seguimiento en Tiempo Real (Tracking): Indicador visual de avance por etapas (Recibido, En evaluación, En liquidación, Cerrado), asignación de liquidador y detalle del caso.
3. Gestión de Evidencias: Captura o selección de imágenes/documentos para adjuntar a las solicitudes pendientes del siniestro.
4. Historial de Gestiones y Avisos: Trazabilidad cronológica de las acciones del liquidador y notificaciones activas.
5. Centro de Ayuda: Preguntas frecuentes interactivas con animaciones fluidas (AnimatedVisibility).

---

## Datos de Prueba (Mock Data en Room)

Para probar el flujo principal (happy path) en un dispositivo o emulador, ingresar con el siguiente usuario registrado en la base de datos local:

* RUT de Prueba: 15.842.371-5 (Camila Andrea Rojas Fuentes)
* Siniestro Activo: SIN-26-004801 (Seguros Cordillera - Colisión)
* Liquidador Asignado: Paulina F. – Equipo Vehículos
