# Proyecto: Procesamiento de Transacciones Bancarias con RabbitMQ

## Descripción del Proyecto

Este proyecto implementa un sistema en **Java 17 con Maven** que procesa
transacciones bancarias utilizando **RabbitMQ** aplicando el patrón
**Producer--Consumer**.

La institución financiera central genera muchas transacciones mediante
una API.\
El sistema desarrollado tiene la responsabilidad de:

-   Obtener las transacciones desde una API externa.
-   Enviar cada transacción a RabbitMQ según el banco destino.
-   Consumir las transacciones desde colas específicas por banco.
-   Guardar cada transacción mediante un endpoint POST.

La idea principal es separar la generación de transacciones del
procesamiento final utilizando colas de mensajería.

------------------------------------------------------------------------

# Arquitectura del Sistema

    GET API (Transacciones)
            │
            ▼
    Producer
    (Java + Maven)
            │
            ▼
    RabbitMQ
    (Colas por banco)
            │
            ▼
    Consumer
    (Java + Maven)
            │
            ▼
    POST API
    (Guardar transacciones)

------------------------------------------------------------------------

# Componentes del Proyecto

El sistema está dividido en dos aplicaciones:

## Producer

El Producer consume el endpoint **GET /transacciones** y publica cada
transacción en RabbitMQ.

Flujo del Producer:

1.  Consume el endpoint GET de la API.
2.  Obtiene un lote de transacciones.
3.  Recorre la lista de transacciones.
4.  Lee el campo `bancoDestino`.
5.  Publica la transacción en una cola cuyo nombre corresponde al banco
    destino.

Ejemplo:

  bancoDestino   Cola RabbitMQ
  -------------- ---------------
  BAC            BAC
  BANRURAL       BANRURAL
  BI             BI
  GYT            GYT

Las colas se crean automáticamente si no existen.

------------------------------------------------------------------------

## Consumer

El Consumer se encarga de consumir los mensajes desde RabbitMQ y
enviarlos al endpoint POST.

Flujo del Consumer:

1.  Consulta el endpoint **GET /transacciones** al iniciar.
2.  Detecta los bancos presentes en el lote.
3.  Escucha las colas correspondientes en RabbitMQ.
4.  Recibe los mensajes en formato JSON.
5.  Convierte los mensajes a objetos Java.
6.  Envía cada transacción al endpoint **POST /guardarTransacciones**.
7.  Confirma el mensaje solo si el POST responde correctamente.

------------------------------------------------------------------------

# Descubrimiento Dinámico de Colas

El Consumer detecta automáticamente qué colas debe escuchar al iniciar.

Proceso:

1.  El Consumer consulta el endpoint GET.
2.  Obtiene los valores de `bancoDestino`.
3.  Genera una lista de bancos.
4.  Empieza a escuchar esas colas en RabbitMQ.

Ejemplo durante la ejecución:

    Colas detectadas: [BANRURAL, GYT, BAC, BI]

Si el GET falla por alguna razón, el sistema usa colas de respaldo:

    BAC
    BANRURAL
    BI
    GYT

Esto permite que el consumer siempre pueda iniciar.

------------------------------------------------------------------------

# Tecnologías Utilizadas

-   Java 17
-   Maven
-   RabbitMQ
-   Jackson para JSON
-   Java HttpClient
-   Docker para ejecutar RabbitMQ

------------------------------------------------------------------------

# Estructura del Proyecto

    banco-proyecto
    │
    ├── producer-bancario
    │   ├── ProducerMain
    │   ├── ProducerService
    │   ├── RabbitPublisher
    │   ├── TransaccionesApiClient
    │   ├── LoteTransacciones
    │   ├── Transaccion
    │   ├── Detalle
    │   └── Referencias
    │
    └── consumer-bancario
        ├── ConsumerMain
        ├── ConsumerService
        ├── RabbitConsumer
        ├── PostTransaccionesApiClient
        ├── TransaccionesApiClient
        ├── LoteTransacciones
        ├── Transaccion
        ├── Detalle
        └── Referencias

------------------------------------------------------------------------

# APIs Utilizadas

## Obtener transacciones

    GET
    https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones

Devuelve un lote con múltiples transacciones.

------------------------------------------------------------------------

## Guardar transacción

    POST
    https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones

Recibe una transacción individual y la almacena en la base de datos.

------------------------------------------------------------------------

# Ejecución del Proyecto

## 1. Iniciar RabbitMQ

Ejecutar RabbitMQ con Docker:

``` bash
docker run -d --hostname rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Panel de administración:

    http://localhost:15672
    usuario: guest
    password: guest

------------------------------------------------------------------------

## 2. Ejecutar Consumer

Primero ejecutar el **Consumer** para que empiece a escuchar las colas.

Salida esperada:

    Escuchando cola: BAC
    Escuchando cola: BANRURAL
    Escuchando cola: BI
    Escuchando cola: GYT
    Consumer iniciado correctamente.

------------------------------------------------------------------------

## 3. Ejecutar Producer

Luego ejecutar el **Producer**.\
Este consumirá el GET y enviará las transacciones a RabbitMQ.

------------------------------------------------------------------------

## 4. Verificar procesamiento

En la consola del Consumer se verá algo como:

    Mensaje recibido desde la cola BAC
    JSON enviado al POST
    Código de respuesta: 201
    ACK enviado para transacción

Esto indica que:

-   el mensaje fue recibido
-   el POST fue exitoso
-   el mensaje fue confirmado

------------------------------------------------------------------------

# Manejo de Errores

El sistema incluye manejo básico de errores.

Producer: - Maneja errores al consumir la API. - Maneja errores de
conexión con RabbitMQ.

Consumer: - Utiliza ACK manual. - Si el POST falla, el mensaje no se
confirma y se vuelve a procesar.

Esto evita la pérdida de transacciones.

------------------------------------------------------------------------

# Características Implementadas

-   Producer funcional
-   Consumer funcional
-   Colas dinámicas por banco
-   Conversión de JSON a objetos Java
-   Confirmación manual de mensajes (ACK)
-   Reintento básico en caso de error
-   Integración con APIs externas

------------------------------------------------------------------------

# Autor

Eduardo Gabriel Visoni Morales\
Carnet: 0905-22-1146

Colas & RabbitMQ --- Programación III
