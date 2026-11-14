# Paso a paso — Entregable 4

Entrega: **martes 17/11 a las 12:00** por Moodle. Pueden ir tachando los pasos a medida que los completan.

---

## Parte A — Repo y primera compilación (integrante 1)

- [ ] **A1.** Descomprimir el proyecto y verificar que compila. Requiere Java 17 o más y Maven.
  ```bash
  cd catalogo-videojuegos
  mvn verify
  ```
  Tiene que terminar con `BUILD SUCCESS` y 22 pruebas OK. Si algo falla, **sacar captura**: va al informe.
- [ ] **A2.** Probar la app con `java -jar web/target/catalogo-web.jar` y abrir http://localhost:8081.
- [ ] **A3.** Crear el repo en GitHub (por ejemplo `catalogo-videojuegos`). Puede ser público, que es más simple para Jenkins.
- [ ] **A4.** Subir el código:
  ```bash
  git init
  git add .
  git commit -m "Estructura inicial: modulos juegos, catalogo y web + Jenkinsfile"
  git branch -M main
  git remote add origin https://github.com/USUARIO/catalogo-videojuegos.git
  git push -u origin main
  ```
- [ ] **A5.** Agregar al compañero como colaborador: Settings → Collaborators.
- [ ] **A6.** (Recomendado) Proteger `main`: Settings → Branches → requerir pull request. Así se puede mostrar el trabajo colaborativo.

## Parte B — Jenkins (integrante 2)

**Importante:** Jenkins tiene que estar en **la laptop que se lleva a la presentación**.

- [ ] **B1.** Instalar Jenkins LTS: https://www.jenkins.io/download/
  - Windows: instalador `.msi`.
  - macOS: `brew install jenkins-lts`.
  - Linux: paquete del sistema.

  Abrir http://localhost:8080, desbloquear con la contraseña inicial e instalar los **plugins sugeridos**.
- [ ] **B2.** Verificar que Jenkins encuentra Java 17 o superior. Si el servicio usa otro Java, configurar `JAVA_HOME` en Manage Jenkins → System → Global properties.
- [ ] **B3.** Configurar Maven: Manage Jenkins → Tools → Maven installations → Add Maven.
  - Name: **`Maven3`** (exacto, lo usa el Jenkinsfile).
  - Marcar "Install automatically".
- [ ] **B4.** Crear el job: New Item → nombre `catalogo-videojuegos` → **Pipeline**.
  - Definition: *Pipeline script from SCM*.
  - SCM: Git. Repository URL: la del repo. Si es privado, agregar credenciales con un *Personal Access Token* de GitHub.
  - Branch: `*/main`.
  - Script Path: `Jenkinsfile`.
- [ ] **B5.** **Build Now** a mano la primera vez. Hace falta para que Jenkins registre el `pollSCM` del Jenkinsfile. Después se dispara solo.
- [ ] **B6.** Abrir http://localhost:8081: la app ya corre desde la carpeta de producción.
  - Windows: `C:\prod\catalogo`
  - Linux/macOS: `~/prod/catalogo`
- [ ] **B7.** Cada error que aparezca: **captura + qué pasó + cómo se arregló** en `docs/errores-pipeline.md`.

## Parte C — Trabajo colaborativo (ambos)

- [ ] **C1.** Cada uno hace al menos un cambio en una rama propia (`feature/...`), lo integra con un pull request a `main` y deja que Jenkins lo construya. Ideas:
  - Integrante 1: agregar un campo "descripción" al juego, con su prueba.
  - Integrante 2: agregar un filtro por plataforma en el catálogo, con su prueba.
- [ ] **C2.** Commits con mensajes claros de **los dos**: el historial de GitHub cuenta como evidencia.

## Parte D — Ensayo de la defensa (ambos)

- [ ] **D1.** Caso 1, éxito: seguir la sección "Demo" del README.
- [ ] **D2.** Caso 2, fallo de pruebas: seguir la sección "Demo" del README. Remarcar que **producción no cambia**.
- [ ] **D3.** Cronometrar. La detección tarda hasta 1 minuto y el pipeline completo unos 1–2 minutos más.
- [ ] **D4.** Preparar 2–3 diapositivas o el diagrama para explicar la arquitectura y el pipeline.

## Parte E — Informe PDF y entrega (ambos)

- [ ] **E1.** Informe con estas secciones:
  - **a. Arquitectura:** módulos, dependencias, diagrama del pipeline y ambientes (desarrollo → Jenkins → producción en carpeta local).
  - **b. Decisiones y justificación:**
    - App de catálogo de videojuegos.
    - Java 17 + Spring Boot.
    - Maven multi-módulo.
    - H2 en archivo, sin instalar una base de datos.
    - Thymeleaf.
    - `pollSCM` en vez de webhook: Jenkins en localhost no es accesible desde GitHub.
    - Carpeta local como producción.
    - Puerto 8081, porque Jenkins usa el 8080.
  - **c. Instructivo de ejecución:** se puede basar en el README y en la Parte B.
  - **d. Errores del pipeline y su solución:** sale de `docs/errores-pipeline.md`.
  - **e. Conclusiones y dificultades.**
- [ ] **E2.** Zip `Entregable4Apellido1Apellido2.zip` con el código (sin carpetas `target/` ni `data/`), el Jenkinsfile, los scripts, el README y el PDF.
- [ ] **E3.** Subir a Moodle antes del martes 17/11 a las 12:00.
