# Distributed Auction System

Un sistema distribuido de subastas en tiempo real implementado en Java que permite a múltiples clientes participar en subastas de productos mediante comunicación TCP y UDP.

> **Nombre del Proyecto:** `distributed-auction-system`  
> **Tecnología:** Java con comunicación TCP/UDP  
> **Tipo:** Sistema distribuido concurrente

## 📋 Descripción

Este proyecto implementa un sistema de subastas distribuido que consta de:

- **Servidor de Subastas**: Gestiona las subastas, recibe pujas y mantiene el estado del sistema
- **Cliente de Subastas**: Interfaz para que los usuarios participen en las subastas
- **Comunicación Dual**: Utiliza tanto TCP para transacciones críticas como UDP para información de estado

## 🏗️ Arquitectura del Sistema

### Componentes Principales

1. **AuctionServer.java**: Servidor principal que gestiona las subastas
2. **AuctionClient.java**: Cliente que permite a los usuarios participar
3. **Product.java**: Modelo de datos para los productos a subastar
4. **AuctionItem.java**: Modelo de datos para las pujas realizadas

### Comunicación

- **TCP (Puerto 12345)**: Para envío de pujas y mensajes críticos
- **UDP (Puerto 9876)**: Para información de estado y heartbeat cada 5 segundos

## 🚀 Características

- ✅ **Subastas en tiempo real** con múltiples clientes concurrentes
- ✅ **Comunicación bidireccional** TCP/UDP
- ✅ **Interfaz gráfica** usando TextIO4GUI
- ✅ **Gestión automática de pujas** con validación de precios
- ✅ **Sistema de heartbeat** para monitoreo de conexiones
- ✅ **Manejo de concurrencia** con hilos separados para envío/recepción
- ✅ **Duración configurable** de subastas (60 segundos por defecto)

## 📦 Requisitos

- **Java 8 o superior**
- **Biblioteca TextIO4GUI** (pcd.util.TextIO4GUI)
- **Red local** para comunicación cliente-servidor

## 🛠️ Instalación y Configuración

### 1. Clonar o descargar el proyecto

```bash
git clone https://github.com/tu-usuario/distributed-auction-system.git
cd distributed-auction-system
```

### 2. Compilar el proyecto

```bash
javac *.java
```

**Nota**: Asegúrate de tener la biblioteca `pcd.util.TextIO4GUI` en tu classpath.

### 3. Ejecutar el servidor

```bash
java AuctionServer
```

### 4. Ejecutar clientes (en terminales separadas)

```bash
java AuctionClient
```

## 📚 Uso del Sistema

### Servidor

1. Ejecuta `AuctionServer`
2. El servidor comenzará a escuchar en los puertos configurados
3. Acepta conexiones de múltiples clientes
4. Gestiona automáticamente las pujas y el tiempo de subasta

### Cliente

1. Ejecuta `AuctionClient`
2. Ingresa tu nombre de usuario cuando se solicite
3. Selecciona las opciones del menú:
   - **1. Enviar puja**: Introduce el monto de tu puja
   - **2. Salir**: Termina la sesión

### Flujo de una Subasta

1. Los clientes se conectan al servidor
2. El servidor inicia una subasta con un producto y precio inicial
3. Los clientes pueden enviar pujas (deben ser mayores a la puja actual)
4. El servidor valida y acepta/rechaza las pujas
5. Todos los clientes reciben actualizaciones en tiempo real
6. La subasta termina después de 60 segundos
7. Se declara el ganador con la puja más alta

## 🔧 Configuración

### Puertos de Red

```java
// En AuctionServer.java y AuctionClient.java
private static final int UDP_SERVER_PORT = 9876;
private static final int TCP_SERVER_PORT = 12345;
```

### Duración de Subasta

```java
// En AuctionServer.java
private static final long AUCTION_DURATION = 60 * 1000; // 60 segundos
```

### Intervalo de Heartbeat

```java
// En AuctionClient.java (MessageSender)
timer.scheduleAtFixedRate(new TimerTask() {
    // ... código ...
}, 0, 5000); // Cada 5 segundos
```

## 📁 Estructura del Proyecto

```
distributed-auction-system/
├── AuctionServer.java    # Servidor principal de subastas
├── AuctionClient.java    # Cliente de subastas
├── Product.java          # Modelo de datos del producto
├── AuctionItem.java      # Modelo de datos de la puja
├── .gitignore           # Exclusiones de Git
└── README.md            # Este archivo
```

## 🧵 Arquitectura de Hilos

### AuctionServer
- **Hilo principal**: Acepta conexiones TCP
- **ClientHandler**: Un hilo por cliente para manejar comunicación TCP
- **UDP Listener**: Hilo para recibir mensajes UDP de heartbeat

### AuctionClient
- **MessageSender**: Envía heartbeat UDP cada 5 segundos
- **MessageReceiver**: Recibe mensajes UDP del servidor
- **Input Thread**: Maneja entrada del usuario
- **Receive Thread**: Recibe mensajes TCP del servidor

## 🐛 Manejo de Errores

- **Desconexión de clientes**: El servidor maneja EOFException automáticamente
- **Errores de red**: Se capturan IOException con mensajes informativos
- **Pujas inválidas**: El servidor valida que las pujas sean mayores a la actual
- **Timeouts**: Sistema de heartbeat para detectar clientes desconectados

## 🔒 Consideraciones de Seguridad

- Validación de pujas en el servidor
- Manejo seguro de excepciones
- No hay autenticación implementada (adecuado para entorno de desarrollo)

## 🚧 Limitaciones Conocidas

- Un solo producto por subasta
- Sin persistencia de datos
- Sin sistema de autenticación
- Configuración de red hardcodeada (localhost)

## 🔮 Posibles Mejoras

- [ ] Base de datos para persistencia
- [ ] Múltiples productos simultáneos
- [ ] Sistema de autenticación
- [ ] Interfaz web
- [ ] Configuración externa (properties)
- [ ] Logging estructurado
- [ ] Métricas y monitoreo

## 👥 Contribución

Este es un proyecto académico. Para contribuir:

1. Fork el repositorio
2. Crea una rama para tu feature
3. Implementa los cambios
4. Envía un pull request

## 📄 Licencia

Proyecto académico - Ver detalles con el instructor del curso.

---

**Nota**: Este sistema fue desarrollado como parte de un proyecto académico de Programación Concurrente y Distribuida (PCD).
