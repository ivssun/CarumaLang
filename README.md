# CarumaLang — Compilador (Léxico y Sintáctico)

## Descripción General
CarumaLang es un lenguaje de programación con una sintaxis personalizada, procesado por un compilador desarrollado en Java utilizando JavaCC. Este proyecto implementa las dos primeras fases fundamentales de un compilador: el **Análisis Léxico** y el **Análisis Sintáctico**, con un enfoque robusto en la detección y recuperación de errores.

---

## ⚙️ Funcionamiento General

El compilador opera mediante un flujo secuencial que transforma el código fuente en una estructura validada. A continuación se describe el ciclo de vida del análisis:

### 1. Fase de Análisis Léxico (Escaneo)
* **Entrada:** Archivo de texto con extensión `.crm`.
* **Proceso:** El analizador lee el flujo de caracteres de entrada y descarta elementos no significativos (espacios, tabulaciones, saltos de línea).
* **Tokenización:** Agrupa los caracteres restantes en unidades lógicas llamadas **Tokens** (palabras reservadas como `Caruma`, operadores como `+`, identificadores, etc.).
* **Manejo de Errores Léxicos:** Si encuentra un carácter que no pertenece al alfabeto del lenguaje (por ejemplo, `@` o `$`), lo registra como un error pero **no detiene la ejecución**. Esto permite reportar todos los caracteres inválidos de una sola vez.

### 2. Fase de Análisis Sintáctico (Parseo)
* **Entrada:** La secuencia de tokens generada por el analizador léxico.
* **Proceso (Parser LL(1)):** El parser solicita tokens al léxico uno a uno y verifica que su orden cumpla con la **Gramática Libre de Contexto** definida. Construye implícitamente un árbol de derivación descendente.
* **Pre-análisis Estructural:** Antes de iniciar el parseo profundo, el sistema realiza un escaneo rápido para verificar exclusivamente el balanceo de delimitadores (llaves `{` y `}`). Esto previene que un bloque mal cerrado genere cientos de errores falsos.
* **Recuperación de Errores (Modo Pánico):** Si el parser encuentra un token inesperado (Error Sintáctico):
    1.  Registra el error en una tabla con su ubicación (línea/columna).
    2.  Entra en estado de "pánico" y descarta tokens hasta encontrar un **punto de sincronización** (como el inicio de una nueva instrucción o un cierre de bloque).
    3.  Retoma el análisis desde ese punto seguro.

---

## Requisitos del Sistema
- **Java JDK:** Versión 8 o superior (recomendado JDK 21).
- **JavaCC:** Archivo `javacc.jar` (incluido en la carpeta `lib`).
- **Variable de Entorno:** `JAVA_HOME` configurada correctamente.

## Estructura del Proyecto
```text
CarumaLang/
├── lib/
│   └── javacc.jar              # Herramienta generadora de parsers
├── src/
│   ├── AnalizadorLexico/       # Definiciones léxicas
│   ├── AnalizadorSintactico/   # Definiciones sintácticas y Gramática
│   │   └── Grammar.jj          # Archivo principal de reglas (Lexer + Parser)
│   ├── AnalisisLexico.java     # Ejecutor independiente para pruebas léxicas
│   └── AnalisisSintactico.java # Ejecutor principal (Parser con recuperación)
└── test/
    ├── prueba.crm              # Código correcto de ejemplo
    ├── errores.crm             # Pruebas de errores léxicos
    ├── errores_sintacticos.crm # Pruebas de errores sintácticos
    └── Calculadora.crm         # Programa complejo de demostración
```

---

## Especificaciones del Lenguaje

### Tokens Principales (Léxico)
| Categoría | Ejemplos | Descripción |
|-----------|----------|-------------|
| **Estructura** | `Caruma`, `byebye` | Inicio y fin del programa. |
| **Tipos** | `intCHELADA`, `granito`, `cadena` | Entero, Decimal, String. |
| **Control** | `CaeCliente`, `papoi`, `paraPapoi` | If, While, For. |
| **Booleanos** | `DIOS`, `DIOSNO` | True, False. |
| **I/O** | `holahola` | Instrucción de impresión. |
| **Dinámicos** | `<MIXCHELADA>`, `<NUMERITO>` | Identificadores y números. |

### Reglas Gramaticales (Sintáctico)
El parser valida estructuras como:
1.  **Programa:** Debe iniciar con `Caruma` y terminar con `byebye`.
2.  **Declaraciones:** `Tipo` + `Identificador` + (`=` + `Expresión` opcional).
3.  **Asignaciones:** `Identificador` + `=` + `Expresión`.
4.  **Control de Flujo:** Estructuras anidadas de `If/Else`, `While` y `For`.
5.  **Expresiones:** Soporte para precedencia matemática (`*` y `/` se evalúan antes que `+` y `-`).

---

## Instrucciones de Compilación y Ejecución

### 1. Generar el Analizador (JavaCC)
Desde la carpeta raíz del proyecto:

**Windows:**
```cmd
cd src\AnalizadorSintactico
java -cp ..\..\lib\javacc.jar javacc Grammar.jj
cd ..\..
```

### 2. Compilar el Código Java
Compila tanto el analizador léxico como el sintáctico:

**Windows:**
```cmd
cd src
javac AnalizadorSintactico\*.java AnalisisSintactico.java AnalisisLexico.java
cd ..
```

### 3. Ejecutar
Puedes ejecutar cualquiera de los dos analizadores según lo que desees probar. Se abrirá una ventana para seleccionar el archivo `.crm`.

**Para Análisis Completo (Recomendado):**
```bash
cd src
java AnalisisSintactico
```

**Para Prueba Solo Léxica:**
```bash
cd src
java AnalisisLexico
```

---