# PARTE 1
# CarumaLang — Analizador Léxico

## Descripción
Analizador léxico para el lenguaje CarumaLang, desarrollado con JavaCC. Este analizador reconoce todos los tokens del lenguaje y **detecta todos los errores léxicos sin detenerse**, proporcionando un reporte completo de tokens válidos y errores encontrados.

## Requisitos
- JDK 21 instalado y variable de entorno JAVA_HOME configurada
- JavaCC (archivo `javacc.jar` en la carpeta `lib`)

## Estructura del Proyecto
```
CarumaLang/
├── lib/
│   └── javacc.jar
├── src/
│   ├── AnalizadorLexico/
│   │   └── Grammar.jj
│   └── AnalisisLexico.java
└── test/
    ├── prueba.crm
    ├── errores.crm
```

## Tokens Implementados

### Palabras Reservadas
- `Caruma` - Inicio del programa
- `holahola` - Instrucción de impresión
- `byebye` - Fin del programa
- `CaeCliente` - Condicional IF
- `SiNoCae` - Else
- `papoi` - Bucle while
- `paraPapoi` - Bucle FOR
- `stopPlease` - Break
- `DIOS` - Valor booleano TRUE
- `DIOSNO` - Valor booleano FALSE
- `intCHELADA` - Tipo entero
- `granito` - Tipo decimal
- `cadena` - Tipo string
- `caracter` - Tipo char

### Operadores
- `=` - Asignación
- `<=` - Menor o igual
- `>=` - Mayor o igual
- `==` - Igualdad
- `>` - Mayor que
- `<` - Menor que
- `+` - Suma
- `-` - Resta
- `*` - Multiplicación
- `/` - División

### Delimitadores
- `(` - Paréntesis de apertura
- `)` - Paréntesis de cierre
- `{` - Llave de apertura
- `}` - Llave de cierre
- `:` - Dos puntos

### Identificadores y Literales
- **Identificadores** - Variables/funciones que comienzan con letra seguida de letras o dígitos
- **Numeritos** - Literales numéricos enteros (ej: `42`) o decimales (ej: `3.14`)
- **TextoLiteral** - Cadenas de texto entre comillas dobles (ej: `"Hola Mundo"`)
- **LetraLiteral** - Caracteres individuales entre comillas simples (ej: `'A'`)

## Instrucciones de Uso

### 1. Generar el Analizador Léxico

Desde la carpeta raíz del proyecto, ejecuta:

**Windows:**
```cmd
cd src\AnalizadorLexico
java -cp ..\..\lib\javacc.jar javacc Grammar.jj
cd ..\..
```

Esto generará los siguientes archivos en `src/AnalizadorLexico/`:
- `CarumaLangLexer.java`
- `CarumaLangLexerConstants.java`
- `CarumaLangLexerTokenManager.java`
- `Token.java`
- `TokenMgrError.java`
- `SimpleCharStream.java`
- `ParseException.java`

### 2. Compilar el Proyecto

**Windows:**
```cmd
cd src
javac AnalizadorLexico\*.java AnalisisLexico.java
cd ..
```

### 3. Ejecutar el Analizador

**Linux/Mac/Windows:**
```bash
cd src
java AnalisisLexico
```

Se abrirá un diálogo para seleccionar un archivo `.crm` de prueba.

## Ejemplo de Uso

### Archivo de entrada (prueba.crm):
```
Caruma
holahola("Hola Mundo desde CarumaLang")

intCHELADA numero = 42
granito pi = 3.14

CaeCliente(numero > 50) {
    holahola("El numero es mayor que 50")
} SiNoCae {
    holahola("El numero es menor o igual a 50")
}

byebye
```

### Salida esperada:
```
========================================
   ANALIZADOR LÉXICO - CARUMALANG
========================================
Archivo: C:\...\prueba.crm

TOKENS RECONOCIDOS:
--------------------------------------------------------------------------------------------------
1     | Caruma                              | "Caruma"                       | Línea: 1, Col: 1
2     | holahola                            | "holahola"                     | Línea: 2, Col: 1
3     | (                                   | "("                            | Línea: 2, Col: 9
4     | "Hola Mundo desde CarumaLang"       | <TEXTOLITERAL>                 | Línea: 2, Col: 10
5     | )                                   | ")"                            | Línea: 2, Col: 38
6     | intCHELADA                          | "intCHELADA"                   | Línea: 4, Col: 1
7     | numero                              | <MIXCHELADA>                   | Línea: 4, Col: 12
8     | =                                   | "="                            | Línea: 4, Col: 19
9     | 42                                  | <NUMERITO>                     | Línea: 4, Col: 21
...
---------------------------------------------------------------------------------------------------

--------------------------------------
        RESUMEN DEL ANÁLISIS
--------------------------------------

Tokens válidos reconocidos: 35
Errores léxicos encontrados: 0

Análisis léxico completado SIN ERRORES
El archivo cumple con la sintaxis léxica de CarumaLang

========================================
```

### Ejemplo con errores (errores.crm):
```
Caruma

intCHELADA x = 10
@ 
intCHELADA y = 20
# 
holahola("test")

byebye
```

### Salida con errores:
```
========================================
   ANALIZADOR LÉXICO - CARUMALANG
========================================
Archivo: C:\...\errores.crm

TOKENS RECONOCIDOS:
--------------------------------------------------------------------------------------------------
1     | Caruma                              | "Caruma"                       | Línea: 1, Col: 1
2     | intCHELADA                          | "intCHELADA"                   | Línea: 3, Col: 1
3     | x                                   | <MIXCHELADA>                   | Línea: 3, Col: 12
4     | =                                   | "="                            | Línea: 3, Col: 14
5     | 10                                  | <NUMERITO>                     | Línea: 3, Col: 16
ERROR | @                                   | Carácter inválido              | Línea: 4, Col: 1
6     | intCHELADA                          | "intCHELADA"                   | Línea: 5, Col: 1
7     | y                                   | <MIXCHELADA>                   | Línea: 5, Col: 12
8     | =                                   | "="                            | Línea: 5, Col: 14
9     | 20                                  | <NUMERITO>                     | Línea: 5, Col: 16
ERROR | #                                   | Carácter inválido              | Línea: 6, Col: 1
...
---------------------------------------------------------------------------------------------------

--------------------------------------
     ERRORES LÉXICOS ENCONTRADOS
--------------------------------------

-----------------------------------------
│ No. │ Carácter    │ Línea  │ Columna │
-----------------------------------------
│ 1    │ @           │ 4      │ 1       │
│ 2    │ #           │ 6      │ 1       │
------------------------------------------

--------------------------------------
        RESUMEN DEL ANÁLISIS
--------------------------------------

Tokens válidos reconocidos: 12
Errores léxicos encontrados: 2

Análisis completado CON ERRORES
Se encontraron 2 caracteres no reconocidos
Revise la tabla de errores para más detalles

========================================
```

## Características Implementadas

### Reconocimiento de Tokens
- Todas las palabras reservadas de CarumaLang
- Operadores aritméticos y relacionales
- Identificadores que comienzan con letra
- Literales numéricos (enteros y decimales)
- Literales de cadena y carácter
- Delimitadores

### Manejo Avanzado de Errores
- **Detección de TODOS los errores léxicos** sin detener el análisis
- Recuperación automática de errores
- Tabla detallada de errores encontrados
- Mensajes descriptivos con línea y columna
- Código ASCII del carácter inválido

### Formato de Salida
- **Columna 1:** Número secuencial del token (o "ERROR" si es inválido)
- **Columna 2:** LEXEMA - texto exacto encontrado en el código fuente
- **Columna 3:** TOKEN - nombre del tipo de token
  - Tokens literales (palabras reservadas): aparecen entre comillas `"Caruma"`
  - Tokens con patrón regex (identificadores, números): aparecen con `<MIXCHELADA>`, `<NUMERITO>`
- **Columna 4:** Ubicación (línea y columna)

### Omisión de Espacios
- Espacios en blanco
- Tabulaciones
- Saltos de línea

## Notas Importantes

1. **Case Sensitive**: CarumaLang es sensible a mayúsculas/minúsculas (`IGNORE_CASE = false`)

2. **Extensión de archivos**: Los archivos de CarumaLang usan la extensión `.crm`

3. **Comentarios**: La versión actual NO implementa comentarios

4. **Orden de tokens**: Los identificadores deben ir DESPUÉS de las palabras reservadas en la gramática para evitar conflictos

5. **Recuperación de errores**: El analizador continúa después de encontrar un error léxico, permitiendo detectar todos los errores en una sola ejecución

## Solución de Problemas

### Error: "Cannot find symbol"
- Verifica que JavaCC generó todos los archivos
- Recompila todos los archivos Java

### Error: "Caracter no reconocido"
- Revisa que el archivo .crm use la sintaxis correcta de CarumaLang
- Verifica que no haya caracteres especiales no soportados (como `@`, `#`, `$`, etc.)
- El analizador mostrará TODOS los caracteres inválidos en la tabla de errores

### JavaCC no genera archivos
- Verifica la ruta al archivo javacc.jar
- Asegúrate de estar en el directorio correcto
- Revisa que Grammar.jj no tenga errores de sintaxis

### No se abre el selector de archivos
- Verifica que tengas un entorno gráfico disponible
- Si estás en un servidor sin GUI, modifica `AnalisisLexico.java` para aceptar argumentos de línea de comandos

## Archivos de Prueba Incluidos

- **prueba.crm**: Programa completo sin errores léxicos
- **errores.crm**: Programa con caracteres inválidos para probar la detección de errores

## Autores
- Renata Carolina Castro Olmos
- Isaías de Jesús Áviles Rodríguez
- Carlos Alberto Ureña Andrade
- Olimpia de los Angeles Moctezuma Juan


# PARTE 2
# CarumaLang - Analizador Sintáctico (Parser LL(1))

## Descripción

Analizador sintáctico descendente recursivo para el lenguaje CarumaLang, desarrollado con JavaCC. Este analizador verifica que la **estructura del programa** sea sintácticamente correcta según las gramáticas libres de contexto LL(1) definidas.

## Requisitos

- JDK 21 instalado y variable de entorno JAVA_HOME configurada
- JavaCC (archivo `javacc.jar` en la carpeta `lib`)

## Estructura del Proyecto

```
CarumaLang/
├── lib/
│   └── javacc.jar
├── src/
│   ├── AnalizadorSintactico/
│   │   └── Grammar.jj              ← Gramática sintáctica LL(1)
│   └── AnalisisSintactico.java      ← Clase principal
└── test/
    ├── prueba.crm                   ← Programa sintácticamente correcto
    ├── errores_sintacticos.crm      ← Programa con errores
    └── Calculadora.crm              ← Programa complejo
```

## Diferencia: Análisis Léxico vs Sintáctico

### Análisis Léxico (Ya implementado)
- **Objetivo**: Reconocer TOKENS individuales
- **Entrada**: Cadena de caracteres
- **Salida**: Secuencia de tokens
- **Gramáticas**: Simples (Tipo 3 - Regulares)
- **Ejemplo**: `intCHELADA x = 10` → `[INTCHELADA] [MIXCHELADA] [ESTOES] [NUMERITO]`

### Análisis Sintáctico (Este módulo)
- **Objetivo**: Verificar la ESTRUCTURA del programa
- **Entrada**: Secuencia de tokens
- **Salida**: Árbol de sintaxis o error sintáctico
- **Gramáticas**: Libres de contexto (Tipo 2 - LL(1))
- **Ejemplo**: Verifica que `intCHELADA x = 10` sea una declaración válida

## Gramáticas Implementadas

### Gramáticas Simples (Análisis Léxico)
1. Identificadores (MIXCHELADA)
2. Números (NUMERITO)
3. Cadenas (TEXTOLITERAL)
4. Caracteres (LETRALITERAL)
5. Operadores aritméticos
6. Operadores relacionales
7. Operadores lógicos (DIOS, DIOSNO)
8. Palabras reservadas
9. Delimitadores
10. Espacios en blanco

### Gramáticas Libres de Contexto LL(1) (Análisis Sintáctico)
1. Programa completo
2. Declaración de variables
3. Asignación
4. Estructura IF (CaeCliente / SiNoCae)
5. Estructura WHILE (papoi)
6. Estructura FOR (paraPapoi)
7. Condiciones
8. Expresiones aritméticas
9. Impresión (holahola)

## Instrucciones de Compilación y Ejecución

### Paso 1: Generar el Parser con JavaCC

Desde la carpeta raíz del proyecto:

**Windows:**
```cmd
cd src\AnalizadorSintactico
java -cp ..\..\lib\javacc.jar javacc Grammar.jj
cd ..\..
```

**Linux/Mac:**
```bash
cd src/AnalizadorSintactico
java -cp ../../lib/javacc.jar javacc Grammar.jj
cd ../..
```

Esto generará los siguientes archivos en `src/AnalizadorSintactico/`:
- `CarumaLangParser.java`
- `CarumaLangParserConstants.java`
- `CarumaLangParserTokenManager.java`
- `Token.java`
- `TokenMgrError.java`
- `SimpleCharStream.java`
- `ParseException.java`

### Paso 2: Compilar el Proyecto

**Windows:**
```cmd
cd src
javac AnalizadorSintactico\*.java AnalisisSintactico.java
cd ..
```

**Linux/Mac:**
```bash
cd src
javac AnalizadorSintactico/*.java AnalisisSintactico.java
cd ..
```

### Paso 3: Ejecutar el Analizador Sintáctico

**Windows:**
```cmd
cd src
java AnalisisSintactico
cd ..
```

**Linux/Mac:**
```bash
cd src
java AnalisisSintactico
cd ..
```

Se abrirá un diálogo para seleccionar un archivo `.crm` de prueba.

## Ejemplos de Uso

### Ejemplo 1: Programa Sintácticamente CORRECTO

**Archivo: test/prueba.crm**
```
Caruma

intCHELADA numero = 42
granito pi = 3.14

CaeCliente(numero > 50) {
    holahola("El numero es mayor que 50")
} SiNoCae {
    holahola("El numero es menor o igual a 50")
}

intCHELADA contador = 0
papoi(contador < 10) {
    holahola("Contador: ")
    holahola(contador)
    contador = contador + 1
}

byebye
```

**Salida esperada:**
```
========================================
   ANALIZADOR SINTACTICO - CARUMALANG
========================================
Archivo: C:\...\prueba.crm

Iniciando analisis sintactico...

========================================
         ANALISIS EXITOSO
========================================

El programa cumple con la sintaxis de CarumaLang
No se encontraron errores sintacticos

========================================
```

### Ejemplo 2: Programa con ERRORES SINTÁCTICOS

**Archivo: test/errores_sintacticos.crm**
```
Caruma

intCHELADA x = 10

CaeCliente(x > 5  
    holahola("error")
}

byebye
```

**Error**: Falta cerrar el paréntesis de la condición

**Salida esperada:**
```
========================================
   ANALIZADOR SINTACTICO - CARUMALANG
========================================
Archivo: C:\...\errores_sintacticos.crm

Iniciando analisis sintactico...

========================================
         ERROR SINTACTICO
========================================

Se encontro un error en la estructura del programa:

Encountered "holahola" at line 5, column 5.
Was expecting:
    ")"

========================================
```

## Tipos de Errores Detectados

### Errores Sintácticos (Detectados por el parser)

1. **Estructura incompleta**
   ```
   CaeCliente(x > 5) {
       holahola("test")
   // ERROR: Falta la llave de cierre }
   ```

2. **Paréntesis no balanceados**
   ```
   holahola("Hola Mundo"
   // ERROR: Falta cerrar paréntesis
   ```

3. **Falta de delimitadores**
   ```
   intCHELADA x = 10
   intCHELADA y  // ERROR: Falta inicialización o continuación
   ```

4. **Palabra reservada faltante**
   ```
   Caruma
   intCHELADA x = 10
   // ERROR: Falta 'byebye' al final
   ```

5. **Condición mal formada**
   ```
   CaeCliente(x) {  // ERROR: Falta operador relacional
       holahola("test")
   }
   ```

6. **Expresión mal formada**
   ```
   x = + 5  // ERROR: Operador sin operando izquierdo
   ```

## Estructura de la Gramática LL(1)

### Características Principales

1. **Sin recursión izquierda**
   ```
   Correcto:   E → T E'
               E' → + T E' | ε
   
   Incorrecto: E → E + T
   ```

2. **Factorización por la izquierda**
   ```
   Correcto:   I → CaeCliente ( C ) { B } I'
               I' → SiNoCae { B } | ε
   
   Incorrecto: I → CaeCliente ( C ) { B }
               I → CaeCliente ( C ) { B } SiNoCae { B }
   ```

3. **LOOKAHEAD limitado**
   - La mayoría de las reglas son LL(1)
   - Solo la inicialización del FOR requiere LOOKAHEAD(2)

### Precedencia de Operadores

| Prioridad | Operadores | Asociatividad |
|-----------|-----------|---------------|
| 1 (mayor) | `*`, `/` | Izquierda |
| 2 (menor) | `+`, `-` | Izquierda |

**Ejemplo:**
```
5 + 3 * 2
```
Se evalúa como: `5 + (3 * 2) = 11` (Correcto)

## Comparación: Léxico vs Sintáctico

### Análisis Léxico
**Entrada:**
```
intCHELADA numero = 42
```

**Salida:**
```
Token 1: "intCHELADA" | INTCHELADA | Línea: 1, Col: 1
Token 2: "numero"     | MIXCHELADA | Línea: 1, Col: 12
Token 3: "="          | ESTOES     | Línea: 1, Col: 19
Token 4: "42"         | NUMERITO   | Línea: 1, Col: 21
```

### Análisis Sintáctico
**Entrada:** Secuencia de tokens del léxico

**Salida:**
```
Programa
└── Declaraciones
    └── DeclaracionVariable
        ├── Tipo (INTCHELADA)
        ├── ListaIdentificadores (numero)
        └── InicializacionOpt
            └── Expresion (42)
```

## Arquitectura del Compilador

```
┌─────────────────┐
│ Código Fuente   │
│   (.crm)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ANALIZADOR      │ ← YA IMPLEMENTADO
│ LÉXICO          │
│ (Tokens)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ANALIZADOR      │ ← ESTE MÓDULO
│ SINTÁCTICO      │
│ (AST)           │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ANALIZADOR      │ ← Futuro
│ SEMÁNTICO       │
└─────────────────┘
```

## Solución de Problemas

### Error: "Cannot resolve symbol CarumaLangParser"
**Solución:**
1. Verifica que JavaCC generó todos los archivos
2. Recompila: `javac AnalizadorSintactico\*.java`

### Error: "Encountered unexpected token"
**Causa:** El código no cumple con la gramática sintáctica

**Solución:**
1. Revisa el mensaje de error para identificar el token inesperado
2. Verifica la estructura del código según la gramática
3. Asegúrate de cerrar todos los bloques y paréntesis

### El parser acepta código incorrecto
**Solución:**
1. Revisa las reglas de la gramática en `Grammar.jj`
2. Verifica que las producciones estén bien definidas
3. Considera si es un error semántico (no sintáctico)

### Error: "LOOKAHEAD specification"
**Causa:** Ambigüedad en la gramática

**Solución:** Ya está resuelto con `LOOKAHEAD(2)` en `Inicializacion()`

## Archivos de Prueba Incluidos

### test/prueba.crm
Programa completo sin errores sintácticos. Incluye:
- Declaración de variables
- Estructuras de control (IF, WHILE)
- Expresiones aritméticas
- Impresión

### test/errores_sintacticos.crm
Programa con errores sintácticos intencionales para probar el parser.

### test/Calculadora.crm
Programa complejo con múltiples estructuras anidadas.

## Notas Importantes

1. **Case Sensitive**: CarumaLang distingue mayúsculas/minúsculas

2. **Extensión de archivos**: Los archivos deben usar `.crm`

3. **Gramática LL(1)**: El parser es descendente recursivo, lo que significa:
   - Análisis de izquierda a derecha
   - Derivación por la izquierda
   - 1 token de anticipación (excepto FOR con 2)

4. **Orden de análisis**:
   - Primero se ejecuta el análisis léxico (automático)
   - Luego se ejecuta el análisis sintáctico
   - Los errores léxicos se detectan antes que los sintácticos

5. **LOOKAHEAD**: Solo se usa en dos lugares:
   - `Declaracion()`: Para distinguir entre declaración y asignación
   - `Inicializacion()`: Para distinguir el tipo en el FOR

## Autores

- Renata Carolina Castro Olmos
- Isaías de Jesús Áviles Rodríguez
- Carlos Alberto Ureña Andrade
- Olimpia de los Angeles Moctezuma Juan

## Referencias

- JavaCC Documentation: https://javacc.github.io/javacc/
- Gramáticas Libres de Contexto: Documentación técnica del proyecto
- Parser LL(1): Teoría de compiladores