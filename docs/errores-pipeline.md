# Registro de errores del pipeline

Completar a medida que aparecen (sirve para la sección "d" del informe). Adjuntar capturas en `docs/capturas/`.

| # | Fecha | Etapa | Error (mensaje) | Causa | Solución | Captura |
|---|---|---|---|---|---|---|
| 1 | 14/11 | Prueba manual de la app (previo a Jenkins) | Después de registrar un juego, la redirección iba a `/;jsessionid=...` y daba 404 | En el primer request sin cookie, Tomcat agrega el id de sesión a la URL (URL rewriting) al hacer `redirect:/`, y la ruta resultante no matchea ningún controlador | Se agregó `server.servlet.session.tracking-modes=cookie` en `web/src/main/resources/application.properties` para usar solo cookies | |
| 2 | 14/11 | Configuración del job (SCM) | `Failed to connect to repository ... returned status code 128` / `Authentication failed for 'https://github.com/Mfernandez1934/catalogo-videojuegos.git/'` | El repo de GitHub era privado y el job no tenía credenciales, así que Jenkins no podía hacer `git ls-remote` | Se cambió la visibilidad del repo a público (alternativa: credencial "Username with password" con un Personal Access Token de GitHub con scope `repo`) | |
| 3 | 14/11 | Deploy a produccion (build #1) | Sin mensaje de error: el build quedó "ejecutando" indefinidamente después de `Version desplegada en C:\prod\catalogo (PID ..., puerto 8081)`, aunque la app respondía `UP` | En Windows, `Start-Process` con `-RedirectStandardOutput` crea el proceso `java` heredando los handles abiertos, entre ellos el pipe de salida del paso `powershell` de Jenkins. Jenkins espera a que ese pipe se cierre, y eso nunca pasa mientras la app corre | Se abortó el build y se cambió `scripts/deploy.ps1` para lanzar la app con `Win32_Process.Create` (`Invoke-CimMethod`), que no hereda handles. El log lo escribe Spring Boot con `--logging.file.name`. Se probó con la salida redirigida por un pipe: el script termina en ~2 s | |
| 4 | 14/11 | Deploy a produccion (detectado al probar el redeploy) | Al redesplegar, la versión anterior seguía corriendo y ocupando el puerto (se habría visto como `Port 8081 was already in use`) | El `java` del PATH era el lanzador de Oracle (`javapath\java.exe`), que arranca otro proceso `java` hijo. `app.pid` guardaba el PID del lanzador; al detenerlo, la app real quedaba huérfana | `deploy.ps1` ahora usa el `java.exe` real (obtenido de la propiedad `java.home`) y, antes de arrancar, detiene cualquier proceso que esté escuchando en el puerto. Se probó con dos deploys seguidos: el PID guardado coincide con el dueño del puerto y no quedan procesos huérfanos | |

## Errores típicos a tener en cuenta (si aparecen, registrarlos arriba)

- **`No tool named Maven3 found`**: Maven no está configurado en Manage Jenkins → Tools con ese nombre exacto.
- **`release version 17 not supported`**: Jenkins está usando un Java más viejo. Configurar `JAVA_HOME`.
- **La app se cae apenas termina el build**: Jenkins mata los procesos hijos. El Jenkinsfile ya usa `JENKINS_NODE_COOKIE=dontKillMe`. Si pasa igual, revisar que esté presente.
- **`Port 8081 was already in use`**: quedó una instancia corriendo sin `app.pid`. Matar el proceso a mano.
  - Windows: `netstat -ano | findstr 8081` y después `taskkill /PID <pid> /F`.
  - Linux/macOS: `lsof -i :8081` y después `kill <pid>`.
- **`bash\r: No such file or directory`**: `deploy.sh` quedó con fin de línea CRLF. Lo evita `.gitattributes`. Si pasa, convertir el archivo a LF.
- **Jenkins no detecta los cambios**: falta el primer build manual, o la URL o la rama del job están mal.
