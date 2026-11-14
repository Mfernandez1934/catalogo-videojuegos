# Catálogo de Videojuegos — Entregable 4 (CI/CD)

Programación Avanzada 2026. Aplicación web simple (registrar videojuegos, clasificarlos por género y mostrar el catálogo con imágenes) cuyo foco es el **pipeline de CI/CD en Jenkins**.

## Arquitectura

Proyecto Maven multi-módulo, Java 17 + Spring Boot 3.3:

```
catalogo-videojuegos/
├── pom.xml                 # POM padre (módulos, versiones, configuración de pruebas)
├── juegos/                 # Módulo 1: entidad Juego, repositorio JPA, reglas de validación
├── catalogo/               # Módulo 2: clasificación por género y búsqueda (usa juegos)
├── web/                    # Módulo 3: app Spring Boot, controlador MVC, vistas Thymeleaf
├── Jenkinsfile             # Pipeline declarativo (pipeline as code)
└── scripts/                # Deploy y health check (bash y PowerShell)
```

Dependencias: `web → catalogo → juegos`.

| Módulo | Pruebas | Tipo |
|---|---|---|
| juegos | `JuegoServiceTest` (9 casos) | Unitarias — JUnit 5 + Mockito (repositorio simulado) |
| catalogo | `CatalogoServiceTest` (7 casos) | Unitarias — JUnit 5 + Mockito (JuegoService simulado) |
| web | `CatalogoWebIT` (6 casos) | Integración — `@SpringBootTest` + MockMvc, app completa con H2 en memoria |

- Unitarias = clases `*Test` → Surefire → `mvn test`
- Integración = clases `*IT` → Failsafe → `mvn verify`
- Persistencia: H2 en archivo (`./data/catalogo` junto al jar). Las pruebas usan H2 en memoria (perfil `test`).

## Requisitos

- Java 17 o superior (`java -version`)
- Maven 3.9 o superior (`mvn -version`), o IntelliJ con Maven incluido
- Jenkins LTS (para el pipeline)

## Ejecutar en local

```bash
mvn verify                                  # compila + unitarias + integración + jar
java -jar web/target/catalogo-web.jar       # levanta la app
```

Abrir http://localhost:8081. La consola de H2 está en http://localhost:8081/h2-console (JDBC URL `jdbc:h2:file:./data/catalogo`, usuario `sa`, sin contraseña).

Otros comandos útiles:

```bash
mvn test                       # solo pruebas unitarias
mvn verify -DskipUTs=true      # solo pruebas de integración
```

## Pipeline (Jenkinsfile)

```
Checkout → Build → Pruebas unitarias → Pruebas de integración → Package → Deploy a producción → Health check
```

- **Disparador:** `pollSCM('* * * * *')`. Jenkins revisa GitHub cada minuto y ejecuta el pipeline si hay commits nuevos.
- **Si falla cualquier etapa**, las siguientes no se ejecutan: producción queda con la versión anterior.
- **Producción simulada:** una carpeta local.
  - Linux/macOS: `~/prod/catalogo`
  - Windows: `C:\prod\catalogo`
  - Se puede cambiar con la variable global `PROD_DIR` en Jenkins.
- **Deploy:** detiene la versión anterior (PID guardado en `app.pid`), copia el jar y lo arranca en segundo plano. Se lanza con `JENKINS_NODE_COOKIE=dontKillMe` para que Jenkins no mate el proceso al terminar el build.
- **Health check:** espera a que `/actuator/health` responda `UP`.

La configuración de Jenkins paso a paso está en [PASO_A_PASO.md](PASO_A_PASO.md).

## Demo para la defensa

**Caso 1 — integración exitosa**
1. Cambiar un texto visible, por ejemplo el título en `web/src/main/resources/templates/catalogo.html` (`Catálogo de Videojuegos` → `Catálogo de Videojuegos v2`).
2. `git commit -am "Cambio de titulo" && git push`
3. En menos de un minuto Jenkins detecta el cambio y corre el pipeline: todo queda en verde.
4. Refrescar http://localhost:8081 y se ve el nuevo título.

**Caso 2 — fallo en las pruebas**
1. Romper una regla, por ejemplo en `juegos/.../JuegoService.java` cambiar `ANIO_MINIMO = 1950` por `ANIO_MINIMO = 1800`. Así falla `registrarConAnioAnteriorAlMinimoFalla`.
2. Hacer commit y push.
3. El pipeline se corta en **Pruebas unitarias**: Deploy y Health check aparecen salteados.
4. Mostrar que http://localhost:8081 **sigue funcionando con la versión anterior**.
5. Mostrar el reporte de pruebas en Jenkins (Test Result), que indica qué prueba falló y por qué.
6. Corregir el error con `git revert HEAD && git push`. El pipeline vuelve a verde y se despliega.
