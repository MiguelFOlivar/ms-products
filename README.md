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
- **Base de datos:** MySQL, esquema `ecommerce`.
- **Comunicación:** APIs REST (JSON).
- **Integración:**
  - **MS-Users:** validación de roles y autenticación.
  - **MS-Orders:** consulta de stock y disponibilidad antes de confirmar pedidos.

---

## 🗄️ Esquema de base de datos

El esquema está definido en [`db/01_create_tables.sql`](db/01_create_tables.sql),
que crea la base de datos `ecommerce` y las siguientes tablas:

| Tabla | Campos y reglas principales |
|---|---|
| `categories` | `id` es la llave primaria autoincremental; `name` es obligatorio y único; `active` es obligatorio y vale `1` por defecto. |
| `products` | `id` es la llave primaria autoincremental; `name`, `price`, `stock` y `category_id` son obligatorios; `description` permite `NULL`; `stock` vale `0` por defecto; `active` vale `1` por defecto; `created_at` recibe la fecha y hora actual por defecto. |
| `promotions` | Almacena el nombre, descuento porcentual, periodo de vigencia y estado de cada promoción. El código valida que el descuento sea mayor que `0` y no supere `50`, y que la fecha inicial sea anterior a la final. |
| `promotion_products` | Relaciona promociones con productos; su clave primaria compuesta evita asociar dos veces el mismo producto a una promoción. |

Cada producto pertenece a una categoría mediante la llave foránea
`products.category_id`, que referencia `categories.id`. Una categoría puede
tener varios productos; la llave foránea no define eliminación en cascada.

Una promoción puede incluir varios productos mediante `promotion_products`.
La base de datos no impide por sí sola que dos promociones vigentes se
superpongan para el mismo producto; esa regla debe validarse al crear o
actualizar promociones en la aplicación. El precio calculado debe redondearse
a dos decimales para respetar la escala de `products.price`.

El esquema también establece `NOT NULL` en los campos obligatorios y `UNIQUE`
para el nombre de categoría. Las reglas de negocio de las promociones se
validan en `PromotionService`, no mediante restricciones `CHECK` en la base de
datos. El precio y el stock tampoco tienen restricciones `CHECK` en el esquema;
su validez debe comprobarse en el código de aplicación.

Para crear la base y las tablas en MySQL, ejecuta desde la raíz del proyecto:

```bash
mysql -u <usuario> -p < db/01_create_tables.sql
```

La configuración de conexión de la aplicación debe apuntar a la misma base de
datos (`ecommerce`) y ser compatible con MySQL.

---

## 🔗 Endpoints

### Promociones
- `POST /api/promotions` (solo ADMIN): crea una promoción para uno o más productos.
- `PUT /api/promotions/{id}` (solo ADMIN): actualiza una promoción; enviar `"active": false` para desactivarla. Si se omite `active`, conserva el estado actual al actualizar.
- `GET /api/promotions/active`: devuelve las promociones vigentes en este momento; las fechas se interpretan con inicio inclusivo y fin exclusivo.

El cuerpo de creación y actualización usa `name`, `discountPercentage`, `startsAt`,
`endsAt` y `productIds`. `active` es opcional (nuevo: activo por defecto). El
descuento acepta valores mayores que 0 y hasta 50, con un máximo de dos decimales.
El precio con descuento se calcula y redondea a dos decimales con `HALF_UP`.
Se rechazan intervalos superpuestos para un mismo producto con HTTP 409.

```json
{
  "name": "Oferta de temporada",
  "discountPercentage": 15.00,
  "startsAt": "2026-11-01T00:00:00",
  "endsAt": "2026-11-08T00:00:00",
  "productIds": [1, 2]
}
```

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

Base de datos: MySQL

Autenticación: JWT

Comunicación: REST APIs
---

## 🧪 Pruebas unitarias

Las pruebas unitarias del servicio de productos se encuentran en
`src/test/java/org/dev/app/msproducts/service/ProductServiceImplTest.java`.
Utilizan JUnit 5 para ejecutar los casos y Mockito para simular el repositorio,
sin conectarse a una base de datos.

Para ejecutar todas las pruebas desde la raíz del proyecto:

```bash
./mvnw test
```

En Windows, utiliza:

```bat
mvnw.cmd test
```

Los casos actuales comprueban:

- Consulta de productos activos y respuesta cuando el catálogo está vacío.
- Consulta por ID, tanto cuando el producto existe como cuando no se encuentra.
- Registro de productos y verificación de los datos persistidos.
- Retiro lógico de productos, incluyendo producto inexistente o ya inactivo.
- Actualización de stock válida y rechazo de stock negativo o nulo, así como de
  productos inexistentes.

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

Editar `src/main/resources/application.properties` con los datos de conexión de
MySQL y la base `ecommerce`:
```
  spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
  spring.datasource.username=<usuario>
  spring.datasource.password=TuPassword
  spring.jpa.hibernate.ddl-auto=none
  spring.jpa.show-sql=true
```
Ejecutar el script de creación de tablas:
```
  mysql -u <usuario> -p < db/01_create_tables.sql
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
   Actualización de prueba por Gerardo - rama feature/update-product 07/10/2026
