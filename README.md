# CarumaLang — Analizador Léxico

## Descripción
Analizador léxico para el lenguaje CarumaLang, desarrollado con JavaCC.

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
    └── prueba.crm
```

## Tokens Implementados

### Palabras Reservadas
- `Caruma` - Inicio del programa
- `holahola` - Declaración de variables
- `byebye` - Fin del programa
- `CaeCliente` - Condicional IF
- `SiNoCae` - Else
- `papoi` - Variable de iteración
- `paraPapoi` - Bucle FOR
- `stopPlease` - Break
- `DIOS` - Salida/Print
- `intCHELADA` - Tipo entero
- `granito` - Tipo decimal
- `cadena` - Tipo string
- `caracter` - Tipo char

### Operadores
- `EstoEs` (=) - Asignación
- `MenorIgualitoque` (<=) - Menor o igual
- `MayorIgualitoque` (>=) - Mayor o igual
- `Igualito` (==) - Igualdad
- `MayorQue` (>) - Mayor que
- `MenorQue` (<) - Menor que
- `Poner` (+) - Suma
- `Quitar` (-) - Resta
- `SaleMas` (*) - Multiplicación
- `SaleMenos` (/) - División

### Delimitadores
- `Abriendo` (() - Paréntesis de apertura
- `Cerrando` ()) - Paréntesis de cierre
- `Open` ({) - Llave de apertura
- `Close` (}) - Llave de cierre
- `AhiVa` (:) - Dos puntos

### Identificadores y Literales
- `mixCHELADA` - Identificadores (variables/funciones)
- `numerito` - Literales numéricos (enteros y decimales)
- `TextoLiteral` - Cadenas de texto entre comillas dobles
- `LetraLiteral` - Caracteres individuales entre comillas simples

## Instrucciones de Uso

### 1. Generar el Analizador Léxico

Desde la carpeta raíz del proyecto, ejecuta:

```bash
cd src/AnalizadorLexico
java -cp ../../lib/javacc.jar javacc Grammar.jj
```

Esto generará los siguientes archivos en `src/AnalizadorLexico/`:
- `CarumaLangLexer.java`
- `CarumaLangLexerConstants.java`
- `CarumaLangLexerTokenManager.java`
- `Token.java`
- `TokenMgrError.java`
- `SimpleCharStream.java`

### 2. Compilar el Proyecto

Regresa a la carpeta `src` y compila todos los archivos Java:

```bash
cd ..
javac AnalizadorLexico/*.java AnalisisLexico.java
```

### 3. Ejecutar el Analizador

```bash
java AnalisisLexico
```

Se abrirá un diálogo para seleccionar un archivo `.crm` de prueba.

## Ejemplo de Uso

### Archivo de entrada (prueba.crm):
```
Caruma
holahola

intCHELADA numero EstoEs 42
cadena mensaje EstoEs "Hola Mundo"

CaeCliente Abriendo numero MayorQue 50 Cerrando Open
    DIOS Abriendo "Mayor que 50" Cerrando
Close

byebye
```

### Salida esperada:
```
========================================
   ANALIZADOR LÉXICO - CARUMALANG
========================================
Archivo: C:\...\prueba.crm

TOKENS RECONOCIDOS:
----------------------------------------
1   | Caruma              | "Caruma"                     | Línea: 1, Col: 1
2   | holahola            | "holahola"                   | Línea: 2, Col: 1
3   | intCHELADA          | "intCHELADA"                 | Línea: 4, Col: 1
4   | numero              | <MIXCHELADA>                 | Línea: 4, Col: 12
5   | EstoEs              | "="                          | Línea: 4, Col: 19
6   | 42                  | <NUMERITO>                   | Línea: 4, Col: 26
...
----------------------------------------
✓ Análisis léxico completado exitosamente.
Total de tokens: 25
========================================
```

## Características Implementadas

### Reconocimiento de Tokens
- Todas las palabras reservadas de CarumaLang
- Operadores aritméticos y relacionales
- Identificadores (mixCHELADA)
- Literales numéricos (enteros y decimales)
- Literales de cadena y carácter
- Delimitadores

### Manejo de Errores
- Detección de caracteres no reconocidos
- Mensajes de error descriptivos
- Información de línea y columna

### Omisión de Espacios
- Espacios en blanco
- Tabulaciones
- Saltos de línea

## Notas Importantes

1. **Case Sensitive**: CarumaLang es sensible a mayúsculas/minúsculas (`IGNORE_CASE = false`)

2. **Extensión de archivos**: Los archivos de CarumaLang usan la extensión `.crm`

3. **Comentarios**: La versión actual NO implementa comentarios. Si necesitas añadirlos, descomenta la sección SKIP en Grammar.jj

4. **Orden de tokens**: Los identificadores (`mixCHELADA`) deben ir DESPUÉS de las palabras reservadas en la gramática para evitar conflictos

## Solución de Problemas

### Error: "Cannot find symbol"
- Verifica que JavaCC generó todos los archivos
- Recompila todos los archivos Java

### Error: "Caracter no reconocido"
- Revisa que el archivo .crm use la sintaxis correcta de CarumaLang
- Verifica que no haya caracteres especiales no soportados

### JavaCC no genera archivos
- Verifica la ruta al archivo javacc.jar
- Asegúrate de estar en el directorio correcto
- Revisa que Grammar.jj no tenga errores de sintaxis

## Autores
Renata Carolina Castro Olmos
Isaías de Jesús Áviles Rodríguez
Carlos Alberto Ureña Andrade
Olimpia de los Angeles Moctezuma Juan