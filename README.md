# PriceStocker

PriceStocker está diseñado para optimizar la gestión de inventarios, permitiendo controlar y consultar sus productos y sus respectivos proveedores, clientes, fabricantes, prestadores de servicios y empresas del mercado. Se ofrece una plataforma integral que facilita el registro de datos y permite un análisis profundo de los precios de compra, venta y mercado, con seguimiento de variaciones, además de generar reportes detallados para la toma de decisiones estratégicas.

## Requerimientos Funcionales

### Gestión de Productos:
- Registro y consulta de productos con detalles como ID, nombre, descripción, existencia y proveedores.
- Los productos pueden tener múltiples precios de compra, venta y mercado, con un historial de cambios y posibilidad de conversión de divisas entre dólares y pesos mexicanos.
- Búsqueda avanzada de productos por nombre, categoría, ID, proveedor, fabricante, cliente, empresa, etc.

### Gestión de Proveedores:
- Registro de proveedores con detalles como ID, nombre, tipo (persona física o jurídica), dirección, RFC, teléfono, correo y CURP.
- Los proveedores pueden tener múltiples categorías y se pueden buscar por diferentes filtros.

### Gestión de Fabricantes:
- Registro de fabricantes con información detallada similar a la de los proveedores.
- Los fabricantes también pueden tener categorías y se podrán realizar búsquedas avanzadas.

### Gestión de Clientes:
- Registro de clientes con datos completos como nombre, RFC, dirección y categorías.
- Los clientes se podrán buscar por filtros y categorías.

### Mercado (Empresas):
- Registro de empresas del mercado que ofrecen productos a precios variables.
- Funciones de búsqueda de empresas por filtros similares a proveedores y clientes.

### Prestadores de Servicios:
- Registro de prestadores con información sobre servicios y rutas que ofrecen.
- Búsqueda avanzada por prestador, ruta, servicio, tipo de servicio, entre otros.

### Funcionalidades Adicionales:
- **Inicio de sesión y gestión de cuentas**: Solo el administrador podrá gestionar cuentas de usuario.

## Especificaciones Técnicas

- **Lenguaje de programación**: Java.
- **Base de datos**: PostgreSQL.
- **Interfaz de usuario**: Se incluirán formularios intuitivos para el registro de datos, y soporte para importar archivos Excel.
