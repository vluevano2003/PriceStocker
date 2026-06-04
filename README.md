# PriceStocker

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green)
![JavaFX](https://img.shields.io/badge/JavaFX-23-blue)
![Build Status](https://github.com/vluevano2003/PriceStocker/actions/workflows/maven.yml/badge.svg)

**PriceStocker** es un sistema integral de gestión de inventarios y administración comercial de escritorio, construido con una arquitectura robusta utilizando **Spring Boot** para el backend y **JavaFX** para una interfaz de usuario moderna y fluida.

---

## 📦 Características Principales

El sistema está diseñado para gestionar el ciclo completo de productos y relaciones comerciales:

* **Internacionalización (i18n):** Soporte multiidioma dinámico (Español e Inglés) integrado de forma nativa en toda la interfaz gráfica, validaciones de negocio, ventanas modales y reportes generados.
* **Control de Acceso Avanzado:** Sistema de Login seguro con gestión de usuarios y roles. Restricción dinámica de vistas y acciones según los permisos (Ej. protección de edición y borrado de catálogo para Cajeros).
* **Alertas de Inventario:** Notificaciones emergentes automáticas y marcadores visuales discretos para identificar de inmediato productos que han caído por debajo de su stock mínimo.
* **Cloud Backup & Sincronización:** Respaldo automatizado de la base de datos en la nube. Incluye creación de cuentas, subida manual, y **Auto-Respaldo silencioso** en segundo plano al cerrar la aplicación.
* **Restauración Inteligente:** Proceso seguro de restauración que descarga, libera conexiones bloqueadas en caliente, y reemplaza la base de datos local automáticamente.
* **Gestión de Movimientos:** Registro completo de **Compras y Ventas** que actualizan automáticamente el **Stock** de los productos en tiempo real.
* **Reportes:** Generación de reportes detallados de compras y ventas en formato PDF.
* **Gestión de Productos:** Catálogo detallado con soporte para categorización y configuración flexible de impuestos (IVA).
* **Precios Dinámicos:** Gestión avanzada de costos y precios por tipo de relación (Cliente, Proveedor, Fabricante, Empresa) con soporte para múltiples monedas (MXN/USD).
* **Gestión de Terceros:** Módulos completos para administrar:
    * Clientes.
    * Proveedores.
    * Fabricantes.
    * Empresas competidoras.
    * Prestadores de Servicios.

---

## 🛠️ Tecnologías Utilizadas
* **Lenguaje:** Java 21 (LTS).
* **Framework:** Spring Boot 3.2.4 (Inyección de dependencias, Transacciones).
* **UI:** JavaFX 23.0.2 (Interfaz gráfica de escritorio).
* **BaaS / Nube:** Supabase (Auth y Cloud Storage para respaldos).
* **Cliente HTTP:** Spring RestClient (Consumo ágil de API REST de Supabase).
* **Internacionalización:** Java ResourceBundle (`.properties`).
* **Base de Datos:** H2 Database (Embebida, modo archivo `.mv.db`).
* **Migraciones:** Flyway (Control de versiones de base de datos).
* **ORM:** Hibernate / Spring Data JPA.
* **Persistencia de Sesión:** `java.util.prefs.Preferences` (Integración nativa con el sistema operativo para preferencias locales).
* **Reportes:** OpenPDF 1.3.30 (Generación de documentos PDF).
* **JUnit 5:** Framework de pruebas.
* **Mockito:** Simulación de dependencias (Mocks).
* **CI/CD:** Configurado con **GitHub Actions** para ejecución automática de pruebas en cada Push/Pull Request.
* **Herramientas:** Maven, Lombok.