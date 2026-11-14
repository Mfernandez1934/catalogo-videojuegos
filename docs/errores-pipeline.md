# Registro de errores del pipeline

Completar a medida que aparecen (sirve para la sección "d" del informe). Adjuntar capturas en `docs/capturas/`.

| # | Fecha | Etapa | Error (mensaje) | Causa | Solución | Captura |
|---|---|---|---|---|---|---|
| 1 | 14/11 | Prueba manual de la app (previo a Jenkins) | Después de registrar un juego, la redirección iba a `/;jsessionid=...` y daba 404 | En el primer request sin cookie, Tomcat agrega el id de sesión a la URL (URL rewriting) al hacer `redirect:/`, y la ruta resultante no matchea ningún controlador | Se agregó `server.servlet.session.tracking-modes=cookie` en `web/src/main/resources/application.properties` para usar solo cookies | |
| 2 | 14/11 | Configuración del job (SCM) | `Failed to connect to repository ... returned status code 128` / `Authentication failed for 'https://github.com/Mfernandez1934/catalogo-videojuegos.git/'` | El repo de GitHub era privado y el job no tenía credenciales, así que Jenkins no podía hacer `git ls-remote` | Se cambió la visibilidad del repo a público (alternativa: credencial "Username with password" con un Personal Access Token de GitHub con scope `repo`) | |
| 3 | | | | | | |

## Errores típicos a tener en cuenta (si aparecen, registrarlos arriba)

- **`No tool named Maven3 found`**: Maven no está configurado en Manage Jenkins → Tools con ese nombre exacto.
- **`release version 17 not supported`**: Jenkins está usando un Java más viejo. Configurar `JAVA_HOME`.
- **La app se cae apenas termina el build**: Jenkins mata los procesos hijos. El Jenkinsfile ya usa `JENKINS_NODE_COOKIE=dontKillMe`. Si pasa igual, revisar que esté presente.
- **`Port 8081 was already in use`**: quedó una instancia corriendo sin `app.pid`. Matar el proceso a mano.
  - Windows: `netstat -ano | findstr 8081` y después `taskkill /PID <pid> /F`.
  - Linux/macOS: `lsof -i :8081` y después `kill <pid>`.
- **`bash\r: No such file or directory`**: `deploy.sh` quedó con fin de línea CRLF. Lo evita `.gitattributes`. Si pasa, convertir el archivo a LF.
- **Jenkins no detecta los cambios**: falta el primer build manual, o la URL o la rama del job están mal.
