# Registro de errores del pipeline

Completar a medida que aparecen (sirve para la sección "d" del informe). Adjuntar capturas en `docs/capturas/`.

| # | Fecha | Etapa | Error (mensaje) | Causa | Solución | Captura |
|---|---|---|---|---|---|---|
| 1 | | | | | | |
| 2 | | | | | | |
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
