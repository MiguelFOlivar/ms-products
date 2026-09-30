<p align="center">
  <img width="600" height="400" alt="depositphotos_679966238-stock-photo-mini-cart-shopping-bags-laptop" src="https://github.com/user-attachments/assets/2b690882-ca38-455f-a07c-52b2355e6cc7" />
</p>


# 🛒 Microservicio de Productos (MS-Products)

## 📌 Descripción
Microservicio de Productos para plataforma **E-COMMERCE**.  
Responsable de la gestión del catálogo, precios, inventario y disponibilidad.  
Se comunica con **MS-Users** (roles/autenticación) y **MS-Orders** (validación de stock en compras).

---

## 🎯 Objetivo
El **MS-Products** gestiona el catálogo de productos dentro de la plataforma E-COMMERCE.  
Permite registrar, consultar, actualizar y eliminar productos, asegurando que la información esté disponible para los clientes y sincronizada con los otros microservicios.

---

## 📂 Responsabilidades
- CRUD de productos.
- Exposición del catálogo para clientes.
- Control de inventario y stock.
- Validación de precios y disponibilidad.
- APIs REST para comunicación con otros microservicios.

---

## ⚙️ Arquitectura
- **Base de datos:** `ProductsDB` (tabla `products`).
- **Comunicación:** APIs REST (JSON).
- **Integración:**
  - **MS-Users:** validación de roles y autenticación.
  - **MS-Orders:** consulta de stock y disponibilidad antes de confirmar pedidos.

---

## 🔗 Endpoints

### Registrar producto
- **Método:** `POST /api/products`
- **Rol requerido:** Admin
- **Body:**
```json
{
  "name": "Laptop",
  "price": 25000,
  "stock": 7
}
```
### Consultar productos
- **Método:** `GET /api/products`
- **Rol requerido:** Cliente/Admin
- **Respuesta:**
```json
[
  {
  "id": 1,
  "name": "Laptop",
  "price": 25000,
  "stock": 7
  },
{
  "id": 2,
  "name": "Laptop Gamer",
  "description": "Laptop con GPU dedicada",
  "price": 25000,
  "stock": 10
}
]

```
### Consultar producto por ID
- **Método:** `GET /api/products/{id}`
- **Rol requerido:** Cliente/Admin
- **Respuesta:**
```json
  {
    "id": 1,
    "name": "Laptop Gamer",
    "price": 25000,
     "stock": 8
  }
```
### Actualizar Producto
- **Método:** `PUT /api/products/{id}`
- **Rol requerido:** Cliente/Admin
- **Body:**
```json
  {
    "price": 25000,
    "available": true
  }
```

### Eliminar producto
- **Método:** `DELETE /api/products/{id}`
- **Rol requerido:** Admin
- **Respuesta:**
```json
  {
  "message": "Producto eliminado correctamente"
  }
```
---
📜 Reglas de negocio
Solo administradores pueden registrar, actualizar o eliminar productos.

Los clientes solo pueden consultar catálogo y detalle.

El stock debe validarse antes de confirmar un pedido (integración con MS-Orders).

Los productos eliminados no deben aparecer en el catálogo.

---

🔗 Integración con otros microservicios
MS-Users: validación de roles y autenticación.

MS-Orders: consulta de stock y disponibilidad antes de procesar compras.
---

🛠️ Tecnologías sugeridas
Backend: Java + Spring Boot

Base de datos: SQL Server

Autenticación: JWT

Comunicación: REST APIs
---

## 🚀 Instalación y ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/MiguelFOlivar/ms-products.git
   cd ms-products
   
Importar el proyecto en IntelliJ IDEA

Abrir IntelliJ → File → Open → seleccionar carpeta ms-products.

Esperar a que se descarguen las dependencias de Maven.

Configurar la base de datos

Editar src/main/resources/application.properties con tus credenciales de SQL Server:
```
  spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ProductsDB
  spring.datasource.username=sa
  spring.datasource.password=TuPassword
  spring.jpa.hibernate.ddl-auto=none
  spring.jpa.show-sql=true
```
Ejecutar el script de creación de tablas:
```
  sqlcmd -S localhost -U sa -P <password> -i db/products_schema.sql
```
Levantar el microservicio

Desde IntelliJ: botón Run en la clase principal.

O desde consola:
```
  mvn spring-boot:run
```
Probar los endpoints

GET http://localhost:8080/api/products

POST http://localhost:8080/api/products

PUT http://localhost:8080/api/products/{id}

DELETE http://localhost:8080/api/products/{id}

```
ms-products/
├── README.md
├── pom.xml
├── src/
│   └── main/
│       ├── java/                # Código fuente
│       └── resources/
│           └── application.properties
├── db/
│   └── products_schema.sql      # Script SQL Server
└── docs/                        # Documentación adicional
```
