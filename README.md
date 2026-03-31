# Programa Taxi BCN 🚖

Proyecto desarrollado para el módulo de Programación (DAM). Este sistema gestiona servicios de taxis normales y adaptados en la ciudad de Barcelona.

Antes de que hagais cualquier virgueria en el codigo, leeros bien todo este texto que obviamente he escrito yo, y que conste que lo he intentado organizar bien, luego no me vengais llorando -S

## 🛠️ Stack Tecnológico
- **Lenguaje:** Java 17+ (Uso de Text Blocks y modernas APIs de String).
- **Testing:** JUnit 5 (Jupiter).
- **IDE Recomendado:** IntelliJ IDEA / Eclipse.
- **Gestión de Versiones:** Git.

## 📂 Estructura del Proyecto (Arquitectura)
Para mantener el código limpio y escalable, utilizamos la siguiente paquetería:
- `com.taxibcn.model`: Entidades y POJOs (`Taxi`, `Customer`, `Person`).
- `com.taxibcn.service`: Lógica de negocio y motores (`ServiceManager`).
- `com.taxibcn.ui`: Interfaz de usuario y menús.
- `com.taxibcn.enums`: Definiciones de estados y tipos.
- `test/`: Carpeta espejo para pruebas unitarias.

## 🤝 Normas de Colaboración (Git)
Como encargado de Git, establezco las siguientes reglas para el equipo:

### 1. Flujo de Ramas (Branching)
- **Prohibido hacer push directamente a `main`**.
- Cada nueva funcionalidad se trabaja en una rama: `feature/nombre-funcionalidad`.
- Antes de mergear a `main`, el código debe ser revisado y debe compilar sin errores.

### 2. Formato de Commits (Conventional Commits)
Para que el historial sea legible, usad estos prefijos:
- `feat:` Nueva funcionalidad (ej: `feat: añadir calculo de tarifas`)
- `fix:` Corrección de errores (ej: `fix: error en el bucle del menú`)
- `refactor:` Mejora de código existente sin cambiar su función.
- `test:` Añadir o modificar pruebas unitarias.

## 🧪 Estrategia de Testing
- **Regla de Oro:** Cada método crítico en `service` debe tener su correspondiente test en la carpeta `test/`.
- No probamos solo el "camino feliz". Es obligatorio testear casos límite (ej: listas vacías, entradas inválidas, falta de taxis disponibles).

---
*Nota: Para cualquier duda sobre la configuración del entorno (.gitignore o librerías JUnit), contactar con el responsable de Git.*