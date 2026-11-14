# Borrador del informe: secciones c y d

> **Para quien arma el informe:** este texto está listo para pasar al Word. Lo que está entre `[CAPTURA: ...]` indica qué imagen va en ese lugar. Están todas en `docs/capturas/` o en el Word de capturas. Los datos (versiones, tiempos, mensajes de error) son los reales del entorno del grupo.

---

## c. Instructivo de ejecución

### c.1 Requisitos

| Herramienta | Versión utilizada | Uso |
|---|---|---|
| Windows | 11 | Sistema operativo del entorno de CI/CD y de producción |
| JDK | 17 o superior (se usó 21 para Jenkins y 22 para desarrollo) | Compilar y ejecutar la aplicación |
| Maven | 3.9 o superior (Jenkins instala 3.10.0 automáticamente) | Build multi-módulo y ejecución de pruebas |
| Git | cualquiera reciente | Control de versiones |
| Jenkins | 2.580.2 LTS (instalador `.msi` para Windows) | Servidor de integración y despliegue continuo |
| IntelliJ IDEA | 2023.3 (opcional) | Desarrollo |

Puertos: Jenkins usa el **8080** y la aplicación el **8081**.

### c.2 Obtener el código

```bash
git clone https://github.com/Mfernandez1934/catalogo-videojuegos.git
cd catalogo-videojuegos
```

Estructura relevante:

```
catalogo-videojuegos/
├── pom.xml          POM padre (módulos juegos, catalogo, web)
├── juegos/          Módulo 1: entidad, repositorio y validaciones
├── catalogo/        Módulo 2: búsqueda, filtros y agrupación
├── web/             Módulo 3: aplicación Spring Boot, controlador y vistas
├── Jenkinsfile      Pipeline declarativo
└── scripts/         deploy.ps1 / deploy.sh y healthcheck.ps1 / healthcheck.sh
```

### c.3 Compilar y probar en local

| Comando | Qué hace |
|---|---|
| `mvn verify` | Compila, ejecuta las 16 pruebas unitarias y las 6 de integración, y genera `web/target/catalogo-web.jar` |
| `mvn test` | Solo pruebas unitarias (clases `*Test`, plugin Surefire) |
| `mvn verify -DskipUTs=true` | Solo pruebas de integración (clases `*IT`, plugin Failsafe) |

Resultado esperado: `BUILD SUCCESS` con 22 pruebas OK (9 de `JuegoServiceTest`, 7 de `CatalogoServiceTest` y 6 de `CatalogoWebIT`).

Sin Maven en el PATH, se puede ejecutar desde IntelliJ: panel **Maven → catalogo-videojuegos → Lifecycle → verify**.

### c.4 Ejecutar la aplicación manualmente (opcional)

```bash
java -jar web/target/catalogo-web.jar
```

- Aplicación: http://localhost:8081
- Consola H2: http://localhost:8081/h2-console (JDBC URL `jdbc:h2:file:./data/catalogo`, usuario `sa`, sin contraseña)
- Estado: http://localhost:8081/actuator/health

No se debe ejecutar así mientras la aplicación de producción esté corriendo, porque ambas usan el puerto 8081.

### c.5 Instalación de Jenkins (Windows)

1. Descargar el instalador **Jenkins 2.580.2 LTS para Windows** (`jenkins.msi`) desde https://www.jenkins.io/download/.
2. Ejecutar el instalador con estas opciones:
   - **Service Logon Credentials:** Run service as LocalSystem.
   - **Port:** 8080.
   - **Java home:** el JDK 21 (`C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot\`).
3. Abrir http://localhost:8080 y desbloquear Jenkins con la contraseña inicial de `C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword`.
4. Elegir **Install suggested plugins** y crear el usuario administrador.

`[CAPTURA: panel principal de Jenkins después de la instalación]`

### c.6 Configuración de Maven en Jenkins

**Administrar Jenkins → Tools → Maven installations → Add Maven**

| Campo | Valor |
|---|---|
| Name | `Maven3` (tiene que coincidir con `tools { maven 'Maven3' }` del Jenkinsfile) |
| Install automatically | Tildado (Install from Apache, versión por defecto) |

### c.7 Creación del job

**Nueva Tarea → nombre `catalogo-videojuegos` → Pipeline**

| Campo | Valor |
|---|---|
| Definition | Pipeline script from SCM |
| SCM | Git |
| Repository URL | `https://github.com/Mfernandez1934/catalogo-videojuegos.git` |
| Credentials | none (el repositorio es público) |
| Branch Specifier | `*/master` |
| Script Path | `Jenkinsfile` |

Después de guardar, ejecutar **Construir ahora** una vez. Ese primer build registra el disparador `pollSCM('* * * * *')` definido en el Jenkinsfile. A partir de ahí, Jenkins revisa GitHub cada minuto y ejecuta el pipeline cuando hay commits nuevos.

`[CAPTURA: configuración del job, sección Pipeline]`

### c.8 Etapas del pipeline

| Etapa | Comando / acción | Si falla |
|---|---|---|
| Checkout | `checkout scm` | Se corta el pipeline |
| Build | `mvn -B clean compile` | Se corta el pipeline |
| Pruebas unitarias | `mvn -B test` + publicación de reportes JUnit (Surefire) | No se ejecutan integración, package ni deploy |
| Pruebas de integración | `mvn -B verify -DskipUTs=true` + reportes (Failsafe) | No se ejecutan package ni deploy |
| Package | Archiva `web/target/catalogo-web.jar` como artefacto del build | No se despliega |
| Deploy a producción | `scripts/deploy.ps1`: detiene la versión anterior, copia el jar a `C:\prod\catalogo` y lo inicia en segundo plano | El health check no se ejecuta |
| Health check | `scripts/healthcheck.ps1`: consulta `/actuator/health` hasta obtener `UP` (máximo 90 s) | El build queda en rojo |

Duración medida: entre **1 min 23 s y 1 min 30 s** por build exitoso (builds #2, #3, #4, #6 y #7). A eso se suma hasta 1 minuto hasta que el pollSCM detecta el commit.

`[CAPTURA: docs/capturas/build2-pipeline-overview.png]`

### c.9 Ambiente de producción simulado

- Carpeta: `C:\prod\catalogo` (se puede cambiar con la variable global `PROD_DIR` en Jenkins).
- Contenido: `catalogo-web.jar`, `app.pid` (PID del proceso en ejecución), `app.log` y `data/` (base H2 en archivo).
- URL: http://localhost:8081.
- La aplicación no arranca sola al reiniciar el equipo. Para levantarla se ejecuta **Construir ahora** en Jenkins.

### c.10 Verificación del flujo (casos de la defensa)

**Caso 1: cambio exitoso.** Se modificó el título en `web/src/main/resources/templates/catalogo.html` (`Catálogo de Videojuegos` → `Catálogo de Videojuegos v2`), se hizo commit y push a `master`. Jenkins detectó el cambio en menos de 1 minuto y ejecutó el build **#4**, que terminó en verde en 1 min 25 s. Al refrescar http://localhost:8081 se vio el título nuevo.

`[CAPTURA: build #4 en verde + página con el título v2]`

**Caso 2: cambio que rompe una prueba.** En `juegos/.../JuegoService.java` se cambió `ANIO_MINIMO = 1950` por `ANIO_MINIMO = 1800`. El build **#5** falló en la etapa **Pruebas unitarias**:

```
[ERROR] Tests run: 9, Failures: 1, Errors: 0, Skipped: 0 -- in com.entregable4.juegos.JuegoServiceTest
[ERROR] JuegoServiceTest.registrarConAnioAnteriorAlMinimoFalla <<< FAILURE!
java.lang.AssertionError: Expecting code to raise a throwable.
```

La prueba registra un juego del año 1900 y espera una `JuegoInvalidoException`. Con el mínimo en 1800, la validación ya no la lanza. Jenkins salteó las etapas *Pruebas de integración*, *Package*, *Deploy a producción* y *Health check*, así que **producción siguió con la versión anterior**. Se corrigió con `git revert HEAD`, y el build **#6** volvió a verde.

`[CAPTURA: build #5 con Pruebas unitarias en rojo y etapas salteadas]`
`[CAPTURA: página Tests del build #5]`
`[CAPTURA: http://localhost:8081 sin cambios después del build #5]`

---

## d. Errores del pipeline y su resolución

Durante la puesta en marcha se registraron cuatro problemas. Se documentaron en `docs/errores-pipeline.md` a medida que aparecían.

### Error 1: Redirección a `/;jsessionid=...` con 404

- **Etapa:** prueba manual de la aplicación, antes de configurar Jenkins.
- **Síntoma:** después de registrar un juego, el navegador era redirigido a `http://localhost:8081/;jsessionid=XXXX` y mostraba un error 404.
- **Causa:** en el primer request del navegador todavía no existe la cookie de sesión. Tomcat aplica entonces *URL rewriting* y agrega el identificador de sesión a la URL del `redirect:/`. La ruta resultante no coincide con ningún controlador.
- **Solución:** se configuró el seguimiento de sesión solo por cookie, en `web/src/main/resources/application.properties`:
  ```properties
  server.servlet.session.tracking-modes=cookie
  ```
- **Aprendizaje:** las pruebas de integración con MockMvc no detectaron el problema porque no simulan el comportamiento del contenedor real sin cookies. Probar la aplicación desplegada sigue siendo necesario.

### Error 2: Jenkins no podía acceder al repositorio

- **Etapa:** configuración del job (sección SCM).
- **Mensaje:**
  ```
  Failed to connect to repository : Command "git.exe ls-remote -h -- https://github.com/Mfernandez1934/catalogo-videojuegos.git HEAD" returned status code 128:
  stderr: fatal: Cannot prompt because user interactivity has been disabled.
  remote: Invalid username or token. Password authentication is not supported for Git operations.
  fatal: Authentication failed for 'https://github.com/Mfernandez1934/catalogo-videojuegos.git/'
  ```
- **Causa:** el repositorio de GitHub era privado y el job no tenía credenciales. Como Jenkins corre como servicio, Git no puede pedir usuario y contraseña de forma interactiva.
- **Solución:** se cambió la visibilidad del repositorio a público. La alternativa, para mantenerlo privado, era crear un *Personal Access Token* de GitHub con permiso `repo` y cargarlo en Jenkins como credencial *Username with password*.

`[CAPTURA: docs/capturas/error2-repo-privado.png]`

### Error 3: El build quedaba colgado en "Deploy a producción"

- **Etapa:** Deploy a producción (build #1).
- **Síntoma:** no había mensaje de error. El log mostraba `Version desplegada en C:\prod\catalogo (PID 8512, puerto 8081)` y la aplicación respondía `UP`, pero la etapa nunca terminaba. Después de unos 10 minutos se abortó el build manualmente (`Finished: ABORTED`).
- **Causa:** el script `deploy.ps1` iniciaba la aplicación con `Start-Process` redirigiendo su salida a archivos (`-RedirectStandardOutput`). En Windows, eso hace que el proceso `java` herede los *handles* abiertos del proceso padre, entre ellos el canal de salida del paso `powershell` de Jenkins. Jenkins espera a que ese canal se cierre para dar por terminado el paso, y eso no ocurre mientras la aplicación siga corriendo.
- **Solución:** la aplicación se inicia con `Win32_Process.Create` (a través de `Invoke-CimMethod`), que crea el proceso sin heredar *handles*. El log lo escribe Spring Boot con `--logging.file.name`. Se verificó ejecutando el script con la salida redirigida por un pipe, igual que en Jenkins: terminó en unos 2 segundos. El build siguiente (#2) completó todas las etapas.
- **Nota:** el Jenkinsfile ya usaba `JENKINS_NODE_COOKIE=dontKillMe`, que evita que Jenkins *mate* la aplicación al terminar, pero no resuelve este problema, que es de herencia de *handles*.

`[CAPTURA: docs/capturas/error3-build1-abortado.png]`

### Error 4: La versión anterior quedaba corriendo después de redesplegar

- **Etapa:** Deploy a producción. Se detectó al probar dos despliegues seguidos, antes de que llegara a ocurrir en Jenkins.
- **Síntoma:** después del segundo despliegue, el PID guardado en `app.pid` ya no existía, pero el puerto seguía ocupado por la versión anterior. En Jenkins se habría visto como `Port 8081 was already in use`.
- **Causa:** el comando `java` del PATH era `C:\Program Files\Common Files\Oracle\Java\javapath\java.exe`, un lanzador que inicia *otro* proceso `java.exe` (el real) como hijo. `app.pid` guardaba el PID del lanzador. Al detenerlo, la aplicación real quedaba huérfana ocupando el puerto.
- **Solución:** `deploy.ps1` ahora:
  1. obtiene el `java.exe` real a partir de la propiedad `java.home` (`java -XshowSettings:properties -version`), y
  2. antes de iniciar la versión nueva, detiene cualquier proceso que esté escuchando en el puerto de la aplicación (`Get-NetTCPConnection`).

  Se verificó con dos despliegues consecutivos: el PID de `app.pid` coincide con el proceso que escucha en el puerto y no quedan procesos huérfanos.

### Resumen

| # | Etapa | Problema | Solución |
|---|---|---|---|
| 1 | App (manual) | Redirect a `;jsessionid` → 404 | `tracking-modes=cookie` |
| 2 | Config. del job | Repo privado sin credenciales | Repo público (alternativa: token) |
| 3 | Deploy | Build colgado por herencia de *handles* | `Win32_Process.Create` |
| 4 | Deploy | App anterior huérfana por el lanzador de Java | `java.exe` real + liberar el puerto |

Después de estas correcciones, todos los builds se dispararon automáticamente por commits y terminaron con el resultado esperado: #2, #3, #4, #6 y #7 en verde, y #5 en rojo a propósito (Caso 2).

`[CAPTURA: docs/capturas/build2-exitoso.png o la vista Stages del job con todos los builds]`
