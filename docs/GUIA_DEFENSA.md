# Procedimiento de demostración

## 1. Preparación del entorno

1. Encender el equipo y esperar a que inicie el servicio de Jenkins.
2. Verificar la conexión a internet.
3. Abrir http://localhost:8080 e iniciar sesión.
4. Job `catalogo-videojuegos` → **Construir ahora**. Esperar resultado verde.
5. Verificar http://localhost:8081 (catálogo visible) y http://localhost:8081/actuator/health (`{"status":"UP"}`).
6. En IntelliJ, terminal en la raíz del repo:
   ```bash
   git checkout master
   git pull
   ```
7. Abrir en IntelliJ:
   - `web/src/main/resources/templates/catalogo.html`
   - `juegos/src/main/java/com/entregable4/juegos/JuegoService.java`
8. Pestañas del navegador:
   1. http://localhost:8080/job/catalogo-videojuegos/
   2. http://localhost:8081
   3. https://github.com/Mfernandez1934/catalogo-videojuegos

Restricción: no ejecutar la aplicación desde IntelliJ (puerto 8081 reservado para producción).

---

## 2. Caso 1: integración y despliegue exitoso

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | http://localhost:8081 | Título `Catálogo de Videojuegos v2` |
| 2 | `catalogo.html`, línea 28: `v2` → `v3`. Guardar (Ctrl+S) | `<h1>Catálogo de Videojuegos v3</h1>` |
| 3 | `git commit -am "Caso 1: cambiar titulo a v3"` | Commit creado |
| 4 | `git push` | `master -> master` |
| 5 | Jenkins → job → lista *Builds* | Build nuevo en ≤ 1 min, origen: "se detectaron cambios en el repositorio" |
| 6 | Clic en el número del build → Pipeline Overview | 7 etapas en verde, ~1 min 30 s |
| 7 | http://localhost:8081 → Ctrl+F5 | Título `Catálogo de Videojuegos v3` |

---

## 3. Caso 2: fallo de pruebas, producción sin cambios

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | `JuegoService.java`: `ANIO_MINIMO = 1950` → `ANIO_MINIMO = 1800`. Guardar (Ctrl+S) | Constante modificada |
| 2 | `git commit -am "Caso 2: cambiar anio minimo"` | Commit creado |
| 3 | `git push` | `master -> master` |
| 4 | Jenkins → build nuevo → Pipeline Overview | *Pruebas unitarias* en rojo. *Pruebas de integracion*, *Package*, *Deploy a produccion* y *Health check* salteadas |
| 5 | Build → **Tests** | Falla `JuegoServiceTest.registrarConAnioAnteriorAlMinimoFalla` — `Expecting code to raise a throwable` |
| 6 | http://localhost:8081 → Ctrl+F5 | Título `v3`, aplicación operativa (versión anterior) |
| 7 | `git revert HEAD --no-edit` | Commit de reversión creado |
| 8 | `git push` | `master -> master` |
| 9 | Jenkins → build nuevo | Todas las etapas en verde |

---

## 4. Referencias

| Elemento | Ubicación |
|---|---|
| Pipeline | `Jenkinsfile` (raíz del repo) |
| Scripts de deploy y health check | `scripts/deploy.ps1`, `scripts/healthcheck.ps1` |
| Producción | `C:\prod\catalogo` (`catalogo-web.jar`, `app.pid`, `app.log`, `data/`) |
| Registro de errores | `docs/errores-pipeline.md` |
| Pruebas unitarias | `juegos/src/test/.../JuegoServiceTest.java` (9), `catalogo/src/test/.../CatalogoServiceTest.java` (7) |
| Pruebas de integración | `web/src/test/.../CatalogoWebIT.java` (6) |
| Consola H2 | http://localhost:8081/h2-console — `jdbc:h2:file:./data/catalogo`, usuario `sa` |

---

## 5. Contingencias

| Situación | Acción |
|---|---|
| Sin build nuevo después de 1 min | Verificar el push y la conexión → **Construir ahora** |
| `git push` rechazado (`rejected`) | `git pull` → `git push` |
| http://localhost:8081 no responde | Jenkins → **Construir ahora** |
| http://localhost:8080 no responde | `services.msc` → Jenkins → Iniciar |
| `Port 8081 was already in use` | Cerrar la instancia local (IntelliJ) → **Construir ahora** |
| Build en ejecución > 5 min | Abortar (X roja) → **Construir ahora** |
| Título sin actualizar | Ctrl+F5 |
