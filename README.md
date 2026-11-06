# TAXI BCN - Sistema de gestión y asisgnación de taxis
Plataforma completa de software desarrollada para TaxiBcn con el fin de optimizar la asignación de vehículos, reducir los tiempos de espera de los usuarios y simular un sistema de transporte en tiempo real eficiente

## 1. Descripción y evolución del proyecto
- **Fases iniciales (SCRUM 1 Y 2):** Se construyó toda la estructura base del motor de asignación mediante interacción por consola.
- **Estado actual:** El proyecto ha evolucionado hacia una solución robusta con interfaz gráfica de usuario (GUI), persistencia real en base de datos relacional y geolocalización basada en mapas reales.

## 2. Características principales
- **Interfaz gráfica:** Controlada mediante una clase principal (AppController) y estructurada con un administrador de diseño CardLayout para alternar entre las vistas de Usuario y Empresa sin congelar la interfaz.
- **Geolocalización:** Integración con la API de Nominatim (OpenStreetMap) para obtener coordenadas geográficas reales a partir de direcciones, procesadas mediante la librería org.json y la clase gestora GeocodingService.
- **Motor matemático:** Cálculo de distancias reales sobre la Tierra implementando la Fórmula de Haversine.
- **Persistencia:** Sistema CRUD completo integrado mediante el Patrón DAO utilizando PreparedStatement para prevenir inyecciones SQL. 

## 3. Arquitectura y decisiones de diseño
- **1. Separación de capas:** Se separó por completo la lógica de negocio de la visualización de datos utilizando POJOs limpios para los modelos.
- **2. Patrón Observer:** Se diseñó la interfaz Sendable para evitar acoplar la lógica con impresiones directas en consola, facilitando la transición limpia hacia la interfaz gráfica.
- **3. Estructuras de datos eficientes:** Uso intensivo de ArrayList dinámicos para manejar listas de espera y servicios activos, combinados con enums para garantizar la consistencia de los estados del sistema.

## 4 Gestión de estado
### Estado del servicio:
- **PENDING:** Al registrar una nueva petición
- **IN_PROGRESS:** Al asignarle un vehículo
- **COMPLETED:** Al finalizar el trayecto (Enviado a ReportManager para métricas y tiempos reales).
### Estado del taxi:
- **AVAILABLE:** Estado inicial o al finalizar un servicio.
- **BUSY:** Al vincularte una solicitud activa.

## 5 Stack tecnológico
- **Lenguaje:** Java
- **Base de datos:** SQLite
- **Geolocalización:** API de OpenStreetMap (Nominatim)
- **Cálculo de distancias:** Fórmula Haversine
- **Interfaz gráfica:** Java Swing
- **Control de versiones:** Git y Github