# Guía para el día de la defensa

Leer de arriba hacia abajo. Cada paso dice **qué hacer** y **qué decir**.

---

## 1. Antes de salir de casa (el día anterior o esa mañana)

- [ ] Cargar la laptop y llevar el cargador.
- [ ] Prender la compu y esperar 2–3 minutos: Jenkins arranca solo (es un servicio de Windows).
- [ ] Abrir http://localhost:8080 y entrar con tu usuario de Jenkins. **Tener la contraseña a mano.**
- [ ] Entrar al job `catalogo-videojuegos` y tocar **Construir ahora**. Esperar a que quede en verde: esto levanta la app de producción, que **no arranca sola al reiniciar la compu**.
- [ ] Abrir http://localhost:8081 y comprobar que se ve el catálogo.

## 2. Al llegar, 10 minutos antes de presentar

- [ ] Conectarse a **internet** (Jenkins revisa GitHub cada minuto; sin internet no detecta los cambios).
- [ ] **No abrir la app desde IntelliJ**: usa el mismo puerto 8081 que producción.
- [ ] En IntelliJ, abrir la terminal (pestaña **Terminal**, abajo) y traer lo último de GitHub:
  ```bash
  git checkout master
  git pull
  ```
- [ ] Si reiniciaste la compu desde el paso 1: **Construir ahora** de nuevo y esperar el verde.
- [ ] Dejar abiertas estas pestañas, en este orden:
  1. Jenkins → job `catalogo-videojuegos` (página principal del job).
  2. http://localhost:8081 (la app en producción).
  3. GitHub → el repo (por si preguntan por commits o PRs).
- [ ] En IntelliJ, dejar abiertos estos dos archivos:
  - `web/src/main/resources/templates/catalogo.html` (Caso 1)
  - `juegos/src/main/java/com/entregable4/juegos/JuegoService.java` (Caso 2)

---

## 3. Explicación inicial (1–2 minutos)

**Qué decir:**
- "Es un catálogo de videojuegos en Java 17 con Spring Boot, dividido en 3 módulos Maven: `juegos` (entidad y validaciones), `catalogo` (búsqueda y filtros por género) y `web` (controlador y vistas)."
- "Hay 16 pruebas unitarias, de `juegos` y `catalogo`, y 6 de integración, que levantan la app completa con una base H2 en memoria."
- "El pipeline está en el `Jenkinsfile` del repo. Jenkins revisa GitHub cada minuto y, si hay un commit nuevo, ejecuta: Checkout → Build → Pruebas unitarias → Pruebas de integración → Package → Deploy a producción → Health check."
- "Producción es una carpeta local, `C:\prod\catalogo`. La app corre en el puerto 8081 porque Jenkins usa el 8080."
- "Si cualquier etapa falla, las siguientes no se ejecutan, así que producción sigue con la versión anterior."

**Qué mostrar:** en Jenkins, tocar el último build en verde → **Pipeline Overview** (el diagrama con todas las etapas).

---

## 4. Caso 1: un cambio que se despliega solo (~3 minutos)

1. **Mostrar el "antes":** pestaña http://localhost:8081. El título dice **"Catálogo de Videojuegos v2"**.
2. **Hacer el cambio:** en IntelliJ, `catalogo.html`, línea 28:
   ```html
   <h1>Catálogo de Videojuegos v2</h1>
   ```
   cambiar a:
   ```html
   <h1>Catálogo de Videojuegos v3</h1>
   ```
   Guardar con **Ctrl+S**.
3. **Commit y push**, en la terminal:
   ```bash
   git commit -am "Caso 1: cambiar titulo a v3"
   git push
   ```
4. **Ir a Jenkins** (página del job). En **menos de 1 minuto** aparece un build nuevo en la lista "Builds", abajo a la izquierda.
   - **Qué decir mientras espera:** "Nadie tocó Jenkins: el pollSCM detectó el commit nuevo en GitHub."
5. **Hacer clic en el número del build nuevo** → se ve el **Pipeline Overview** avanzando.
   - **Qué decir:** ir nombrando las etapas a medida que se ponen en verde. Tarda **~1 min 30 s**.
6. Cuando todo esté en verde, ir a http://localhost:8081 y refrescar con **Ctrl+F5**: el título dice **"v3"**.
   - **Qué decir:** "El cambio pasó todas las pruebas y se desplegó automáticamente en producción."

---

## 5. Caso 2: un cambio que rompe una prueba (~3 minutos)

1. **Hacer el cambio:** en IntelliJ, `JuegoService.java`, buscar (Ctrl+F) `ANIO_MINIMO = 1950` y cambiarlo a:
   ```java
   ANIO_MINIMO = 1800
   ```
   Guardar con **Ctrl+S**.
   - **Qué decir:** "Cambio una regla de negocio: el año mínimo de un juego. Hay una prueba unitaria que verifica esa regla."
2. **Commit y push:**
   ```bash
   git commit -am "Caso 2: cambiar anio minimo (rompe una prueba)"
   git push
   ```
3. **Ir a Jenkins** → esperar el build nuevo → **hacer clic en su número**.
4. En el Pipeline Overview se ve:
   - **Pruebas unitarias en rojo.**
   - **Pruebas de integración, Package, Deploy a producción y Health check salteadas** (en gris).
   - **Qué decir:** "El pipeline se cortó en las pruebas unitarias. El deploy nunca se ejecutó."
5. **Mostrar qué prueba falló:** en el build, tocar **Tests** (arriba). Falla `registrarConAnioAnteriorAlMinimoFalla`.
   - **Qué decir:** "Jenkins muestra exactamente qué prueba falló y por qué."
6. **Mostrar que producción no cambió:** http://localhost:8081 → **Ctrl+F5**. Sigue en **"v3"** y funciona.
   - **Qué decir (lo más importante):** "**Producción no se modificó**: sigue corriendo la última versión buena."
7. **Arreglar el error** (revertir el commit que rompió):
   ```bash
   git revert HEAD --no-edit
   git push
   ```
   Esperar el build nuevo: vuelve a quedar **todo en verde**.
   - **Qué decir:** "Con el revert, el pipeline vuelve a verde y se despliega la versión corregida."

---

## 6. Preguntas que pueden hacer (respuestas cortas)

| Pregunta | Respuesta |
|---|---|
| ¿Por qué pollSCM y no webhook? | Jenkins corre en localhost y GitHub no puede llegar a él desde internet. pollSCM revisa cada minuto. |
| ¿Dónde está el pipeline? | En el `Jenkinsfile` en la raíz del repo: es *pipeline as code*, versionado con el código. |
| ¿Qué es producción? | Una carpeta local, `C:\prod\catalogo`. El script `scripts/deploy.ps1` detiene la versión anterior, copia el jar nuevo y lo arranca. |
| ¿Cómo sabe que el deploy funcionó? | La etapa Health check consulta `/actuator/health` hasta que responde `UP` (máximo 90 s). |
| ¿Diferencia entre pruebas unitarias e integración? | Las unitarias prueban un servicio con sus dependencias simuladas (Mockito). Las de integración levantan la app completa con H2 en memoria y prueban las pantallas con MockMvc. |
| ¿Qué errores tuvieron? | 4, documentados en `docs/errores-pipeline.md`. El más interesante: el build quedaba colgado en Deploy porque la app heredaba la salida de Jenkins. Se resolvió lanzándola desacoplada. |
| ¿Por qué H2? | No hay que instalar una base de datos. En archivo para producción, en memoria para las pruebas. |
| ¿Por qué el puerto 8081? | El 8080 lo usa Jenkins. |

---

## 7. Si algo sale mal

| Problema | Qué hacer |
|---|---|
| Pasó más de 1 minuto y no aparece el build nuevo | Revisar que el `git push` haya funcionado y que haya internet. Si no, tocar **Construir ahora**. |
| `git push` da error "rejected" | Alguien subió algo antes. Ejecutar `git pull` y después `git push` de nuevo. |
| http://localhost:8081 no carga | Producción no está corriendo (¿se reinició la compu?). Jenkins → **Construir ahora** y esperar el verde. |
| http://localhost:8080 no carga | Jenkins está apagado. Tecla Windows → escribir **Servicios** → buscar **Jenkins** → clic derecho → **Iniciar**. |
| El Deploy falla con "Port 8081 already in use" | Quedó la app abierta desde IntelliJ. Cerrarla y tocar **Construir ahora**. |
| El build queda colgado más de 5 minutos | Abortarlo con la **X roja** y tocar **Construir ahora**. |
| Después del Caso 1 el título no cambia | Refrescar con **Ctrl+F5** (el navegador guardó la versión vieja). |

**Regla de oro:** si algo se traba, no improvisar. **Construir ahora** en Jenkins resuelve casi todo.
