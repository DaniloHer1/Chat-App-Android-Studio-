# FastChat

Aplicación de mensajería instantánea para Android desarrollada con Java, Firebase y Material Design 3. Implementa autenticación con Google Sign-In, mensajería en tiempo real y un sistema innovador de cambio automático de tema basado en sensores de luz ambiente.

## Características Principales

### Autenticación y Seguridad
- Autenticación mediante Google Sign-In
- Gestión segura de sesiones con Firebase Authentication
- Verificación automática de estado de sesión al iniciar la aplicación

### Mensajería en Tiempo Real
- Chat individual entre usuarios registrados
- Sincronización instantánea de mensajes mediante Firestore
- Interfaz de conversación moderna con burbujas de mensaje diferenciadas
- Indicadores de tiempo en cada mensaje
- Scroll automático a mensajes nuevos

### Interfaz de Usuario
- Diseño basado en Material Design 3
- Splash screen animado con soporte para GIF
- Perfiles de usuario con fotografías circulares mediante Glide
- Lista de usuarios disponibles para chat
- Barra de herramientas personalizada con información del usuario

### Sistema de Temas Automático
- Cambio dinámico entre modo claro y oscuro
- Detección automática basada en sensor de luz ambiente
- Umbrales configurables para transición de temas
- Persistencia de preferencias mediante SharedPreferences
- Aplicación inmediata sin reinicio de actividad

## Tecnologías Utilizadas

### Lenguaje y Framework
- Java 11
- Android SDK 24-36 (Nougat a Android 14)
- Gradle Kotlin DSL

### Firebase Services
- Firebase Authentication
- Cloud Firestore
- Firebase BOM 34.5.0
- Firebase Analytics

### Bibliotecas de Terceros
- Google Play Services Auth 21.0.0
- Glide 4.16.0 (carga y caché de imágenes)
- CircleImageView 3.1.0 (imágenes de perfil circulares)
- Material Components para Android

### Herramientas de Desarrollo
- Android Studio
- Firebase Console
- Google Cloud Console

## Estructura del Proyecto

```
app/src/main/java/
├── activities/
│   ├── SplashActivity.java      # Pantalla de inicio con animación
│   ├── LoginActivity.java       # Autenticación con Google
│   ├── HomeActivity.java        # Lista de usuarios disponibles
│   └── ChatActivity.java        # Interfaz de conversación
├── adapters/
│   ├── UserAdapter.java         # Adaptador para lista de usuarios
│   └── MessageAdapter.java      # Adaptador para mensajes del chat
├── models/
│   ├── User.java                # Modelo de datos de usuario
│   └── Message.java             # Modelo de datos de mensaje
└── utils/
    └── LightSensorManager.java  # Gestión del sensor de luz

app/src/main/res/
├── layout/
│   ├── activity_splash.xml
│   ├── activity_login.xml
│   ├── activity_home.xml
│   ├── activity_chat.xml
│   ├── item_user.xml
│   ├── item_message_sent.xml
│   └── item_message_received.xml
├── values/
│   ├── colors.xml               # Colores para modo claro
│   ├── themes.xml
│   └── strings.xml
└── values-night/
    ├── colors.xml               # Colores para modo oscuro
    └── themes.xml
```

## Requisitos Previos

- Android Studio Arctic Fox o superior
- JDK 11 o superior
- Cuenta de Google para Firebase
- Dispositivo Android con API 24 o superior
- Sensor de luz ambiente (para funcionalidad de tema automático)

## Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/fastchat.git
cd fastchat
```

### 2. Configurar Firebase

1. Crear un proyecto en [Firebase Console](https://console.firebase.google.com/)
2. Agregar una aplicación Android con el package name: `daniel.chatapp`
3. Descargar el archivo `google-services.json`
4. Colocar el archivo en el directorio `app/`

### 3. Configurar Google Sign-In

1. En Firebase Console, habilitar el método de autenticación "Google"
2. Generar SHA-1 del keystore:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
3. Agregar el SHA-1 en la configuración de la app en Firebase
4. Verificar que el Web Client ID esté correctamente configurado en `LoginActivity.java`

### 4. Configurar Firestore

1. En Firebase Console, crear una base de datos Firestore
2. Configurar las siguientes reglas de seguridad:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /chats/{chatId}/messages/{messageId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 5. Compilar y Ejecutar

1. Abrir el proyecto en Android Studio
2. Sincronizar Gradle
3. Conectar un dispositivo Android o iniciar un emulador
4. Ejecutar la aplicación

## Uso de la Aplicación

### Primera Vez

1. La aplicación muestra un splash screen animado
2. Si no hay sesión activa, se redirige a la pantalla de login
3. Iniciar sesión con una cuenta de Google
4. El usuario se registra automáticamente en Firestore

### Navegación

1. **HomeActivity**: Lista de usuarios registrados disponibles para chat
2. **ChatActivity**: Al seleccionar un usuario, se abre la conversación
3. **Envío de mensajes**: Escribir en el campo de texto y presionar el botón enviar
4. **Cerrar sesión**: Botón en la esquina superior derecha de HomeActivity

### Tema Automático

- El sistema detecta la luz ambiente mediante el sensor
- Umbral de oscuridad: 100 lux (cambia a modo oscuro)
- Umbral de claridad: 150 lux (cambia a modo claro)
- Delay de cambio: 5 segundos (evita cambios rápidos)
- El tema se persiste entre sesiones

## Arquitectura

### Patrones de Diseño

- **MVC Simplificado**: Separación entre modelos, vistas y lógica
- **ViewHolder Pattern**: En adaptadores de RecyclerView
- **Singleton**: FirebaseAuth y FirebaseFirestore
- **Observer Pattern**: Listeners de Firestore para tiempo real

### Flujo de Datos

```
Usuario → Activity → Firebase Auth → Firestore
                                    ↓
                        Snapshot Listener (Tiempo Real)
                                    ↓
                        Adapter → RecyclerView → UI
```

### Gestión de Ciclo de Vida

- Registro/desregistro de listeners en onResume/onPause
- Verificación de contexto válido antes de operaciones con Glide
- Cancelación de handlers en onDestroy
- Limpieza de recursos del sensor de luz

## Características Técnicas Destacadas

### Gestión de Mensajes en Tiempo Real

- Uso de `addSnapshotListener` para sincronización instantánea
- Ordenamiento por timestamp ascendente
- Detección de cambios con `DocumentChange.Type.ADDED`
- Inserción eficiente con `notifyItemInserted`

### Optimización de Imágenes

- Carga asíncrona con Glide
- Caché automático de imágenes de perfil
- Placeholders durante la carga
- Imágenes de error predeterminadas
- Verificación de contexto para prevenir memory leaks

### Sensor de Luz Ambiente

- Implementación de `SensorEventListener`
- Umbrales con histéresis para evitar parpadeo
- Delay configurable entre cambios
- Persistencia de estado con SharedPreferences
- Aplicación inmediata con `AppCompatDelegate.setDefaultNightMode()`

## Problemas Conocidos y Soluciones

### Problema: Google Sign-In no funciona en emulador
**Solución**: Usar dispositivo físico o emulador con Google Play Services instalado

### Problema: Sensor de luz no disponible
**Solución**: El tema se mantiene en modo claro por defecto. Funcionalidad solo disponible en dispositivos con sensor.

### Problema: Mensajes duplicados
**Solución**: Implementado sistema de IDs únicos y verificación antes de agregar

## Mejoras Futuras

- Notificaciones push para mensajes nuevos
- Estado de "escribiendo..."
- Confirmación de lectura de mensajes
- Envío de imágenes y archivos
- Chat grupal
- Búsqueda de mensajes
- Respuestas citadas
- Mensajes de voz

## Dependencias del Proyecto

```gradle
dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    
    // UI
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.11.0")
    
    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
```

## Autor

Daniel Hernando - Proyecto académico de desarrollo Android

