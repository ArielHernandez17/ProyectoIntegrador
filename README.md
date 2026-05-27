# Proyecto Integrador: Gestión de Datos y Almacenamiento Android

Este proyecto es una aplicación de Android desarrollada en Java que integra tres funcionalidades clave de gestión de información: persistencia en memoria interna, manejo de archivos en almacenamiento externo (SD/USB OTG) y administración de bases de datos locales con SQLite.

## Autor
* **Estudiante:** Ariel Hernández Velázquez
* **Carrera:** Ingeniería en Computación
* **Materia:** Programación Movil

## Funcionalidades Principaless

### 1. Bloc de Notas (Persistencia Interna)
* Permite escribir y guardar notas de texto en la memoria privada de la aplicación.
* Implementa un contador de caracteres en tiempo real.
* Carga automática de la última nota guardada al iniciar la sección.

### 2. Gestión de Archivos SD / USB OTG (Almacenamiento Externo)
* **Detección Automática:** Intenta identificar rutas de almacenamiento externo físico.
* **Modo Manual (SAF):** Implementa el *Storage Access Framework* de Android para permitir al usuario seleccionar manualmente carpetas en adaptadores USB-C o tarjetas SD, superando las restricciones de *Scoped Storage* en versiones modernas de Android (11+).
* **Recuperación Inteligente:** Selector de archivos integrado para cargar documentos desde cualquier ubicación del dispositivo.

### 3. Inventario SQLite (Base de Datos)
* CRUD completo (Alta, Baja, Consulta y Modificación) de artículos.
* Atributos: Código, Descripción, Precio y Stock.
* **Sistema de Alertas:** Genera notificaciones de sistema (Push Notifications) cuando un artículo tiene un stock crítico (menor a 5 unidades).
* Visualización dinámica de todos los registros mediante un ListView.

##  Tecnologías Utilizadas
* **Lenguaje:** Java
* **IDE:** Android Studio
* **Base de Datos:** SQLite
* **SDK Mínimo:** API 24 (Android 7.0)
* **SDK Objetivo:** API 34+
* **Componentes UI:** Material Design, CardView, GridLayout y adaptadores personalizados.

##  Requisitos para Instalación
1. Clonar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Para la función de SD en dispositivos modernos, asegúrese de tener activada la función **OTG** en los ajustes de su teléfono si utiliza un adaptador USB-C.

## Estructura del Proyecto
* `MainActivity.java`: Lógica principal de navegación y gestión de archivos.
* `AdminSQLiteOpenHelper.java`: Gestión de la estructura de la base de datos SQLite.
* `activity_main.xml`: Diseño de interfaz basado en tarjetas y menús dinámicos.
