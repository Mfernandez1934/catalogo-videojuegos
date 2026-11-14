# Paso a paso — Entregable 4

Entrega: **martes 17/11 a las 12:00** por Moodle. Pueden ir tachando los pasos a medida que los completan.

## Estado y reparto (actualizado 14/11)

- **Integrante 1** (dueño del repo): instala y configura **Jenkins en su compu** (Parte B). La app de producción corre ahí y esa compu se usa en la defensa.
- **Integrante 2**: clona el repo, hace su cambio en una rama con PR (C1) y arranca el informe (E1 a, b, e).
- **Ya hecho:** compila con 22 pruebas OK, repo en GitHub (rama `master`), fix del `jsessionid` y primer PR mergeado (`feature/readme-ramas`).

---

## Parte A — Repo y primera compilación (integrante 1)

- [x] **A1.** Verificar que compila con `mvn verify`: `BUILD SUCCESS` y 22 pruebas OK. Sin Maven en el PATH se puede correr desde IntelliJ: pestaña Maven → Lifecycle → verify.
- [x] **A2.** Probar la app con `java -jar web/target/catalogo-web.jar` y abrir http://localhost:8081.
- [x] **A3.** Repo en GitHub: https://github.com/Mfernandez1934/catalogo-videojuegos (rama `master`).
- [x] **A4.** Código subido. El proyecto está en la **raíz** del repo, que es donde Jenkins busca el `Jenkinsfile` y el `pom.xml`.
- [ ] **A5.** Agregar al compañero como colaborador: Settings → Collaborators → Add people.
- [ ] **A6.** (Opcional) Proteger `master` exigiendo pull request. **Ojo:** así no se puede hacer `git push` directo a `master` en la demo; habría que hacer los Casos 1 y 2 con PR o desactivar la protección antes.

## Parte B — Jenkins (integrante 1, en su compu)

Esta compu tiene varios JDK. Jenkins tiene que usar el **JDK 21** (`C:\Program Files\Eclipse Adoptium\jdk-21.0.2.13-hotspot`), porque Jenkins no soporta oficialmente el 22.

- [ ] **B1.** Instalar Jenkins LTS con el `.msi` para Windows: https://www.jenkins.io/download/
  - Service Logon Credentials: **Run service as LocalSystem**.
  - Puerto: **8080** ("Test Port").
  - Java home: el **JDK 21** de arriba.
- [ ] **B2.** Abrir http://localhost:8080. La contraseña inicial está en `C:\ProgramData\Jenkins\.jenkins\secrets\initialAdminPassword`. Después **Install suggested plugins** y crear el usuario admin.
- [ ] **B3.** Configurar Maven: Manage Jenkins → Tools → Maven installations → Add Maven.
  - Name: **`Maven3`** (exacto, lo usa el Jenkinsfile).
  - Marcar "Install automatically".
- [ ] **B4.** Crear el job: New Item → nombre `catalogo-videojuegos` → **Pipeline**.
  - Definition: *Pipeline script from SCM*.
  - SCM: Git. Repository URL: `https://github.com/Mfernandez1934/catalogo-videojuegos.git`.
  - Branch: `*/master`.
  - Script Path: `Jenkinsfile`.
- [ ] **B5.** **Build Now** a mano la primera vez. Hace falta para que Jenkins registre el `pollSCM` del Jenkinsfile. Después se dispara solo.
- [ ] **B6.** Abrir http://localhost:8081: la app ya corre desde la carpeta de producción `C:\prod\catalogo`.
  - **No correr la app desde IntelliJ al mismo tiempo**: las dos usan el puerto 8081 y el deploy falla.
- [ ] **B7.** Cada error que aparezca: **captura + qué pasó + cómo se arregló** en `docs/errores-pipeline.md`.

## Parte C — Trabajo colaborativo (ambos)

- [ ] **C1.** Cada uno hace al menos un cambio en una rama propia (`feature/...`) y lo integra con un pull request a `master`.
  - Integrante 1: ya hizo `feature/readme-ramas` (PR #1).
  - Integrante 2: `feature/filtro-plataforma`. Agregar en `CatalogoService` un método que filtre por plataforma (`Juego` ya tiene el campo `plataforma`), con al menos una prueba en `CatalogoServiceTest`. Abrir el PR **sin mergear**: se mergea cuando Jenkins esté andando, como primera prueba del disparo automático.
- [ ] **C2.** Commits con mensajes claros de **los dos**: el historial de GitHub cuenta como evidencia.

## Parte D — Ensayo de la defensa (ambos, en la compu del integrante 1)

- [ ] **D1.** Caso 1, éxito: seguir la sección "Demo" del README.
- [ ] **D2.** Caso 2, fallo de pruebas: seguir la sección "Demo" del README. Remarcar que **producción no cambia**.
- [ ] **D3.** Cronometrar. La detección tarda hasta 1 minuto y el pipeline completo unos 1–2 minutos más.
- [ ] **D4.** Preparar 2–3 diapositivas o el diagrama para explicar la arquitectura y el pipeline.

## Parte E — Informe PDF y entrega (ambos)

- [ ] **E1.** Informe con estas secciones:
  - **a. Arquitectura** (integrante 2): módulos, dependencias, diagrama del pipeline y ambientes (desarrollo → Jenkins → producción en carpeta local).
  - **b. Decisiones y justificación** (integrante 2):
    - App de catálogo de videojuegos.
    - Java 17 + Spring Boot.
    - Maven multi-módulo.
    - H2 en archivo, sin instalar una base de datos.
    - Thymeleaf.
    - `pollSCM` en vez de webhook: Jenkins en localhost no es accesible desde GitHub.
    - Carpeta local como producción.
    - Puerto 8081, porque Jenkins usa el 8080.
  - **c. Instructivo de ejecución** (integrante 1, después de instalar Jenkins): se basa en el README y en la Parte B.
  - **d. Errores del pipeline y su solución** (integrante 1): sale de `docs/errores-pipeline.md`.
  - **e. Conclusiones y dificultades** (borrador del integrante 2, lo cierran entre los dos).
- [ ] **E2.** Zip `Entregable4Apellido1Apellido2.zip` con el código (sin carpetas `target/` ni `data/`), el Jenkinsfile, los scripts, el README y el PDF.
- [ ] **E3.** Subir a Moodle antes del martes 17/11 a las 12:00.
