# PriceStocker

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-green)
![JavaFX](https://img.shields.io/badge/JavaFX-23-blue)
![Build Status](https://github.com/vluevano2003/PriceStocker/actions/workflows/maven.yml/badge.svg)

**PriceStocker** es un sistema integral de gestión de inventarios y administración comercial de escritorio, construido con una arquitectura robusta utilizando **Spring Boot** para el backend y **JavaFX** para una interfaz de usuario moderna y fluida.

---

## 📦 Características Principales

El sistema está diseñado para gestionar el ciclo completo de productos y relaciones comerciales:

* **Control de Acceso:** Sistema de Login seguro con gestión de usuarios y permisos.
* **Gestión de Productos:** Catálogo detallado con soporte para categorización.
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
* **Base de Datos:** H2 Database (Embebida, modo archivo `.mv.db`).
* **Migraciones:** Flyway (Control de versiones de base de datos).
* **ORM:** Hibernate / Spring Data JPA.
* **JUnit 5:** Framework de pruebas.
* **Mockito:** Simulación de dependencias (Mocks).
* **CI/CD:** Configurado con **GitHub Actions** para ejecución automática de pruebas en cada Push/Pull Request.
* **Herramientas:** Maven, Lombok.
