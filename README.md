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