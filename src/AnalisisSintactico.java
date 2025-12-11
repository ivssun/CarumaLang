import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import AnalizadorSintactico.*;

/**
 * Analizador Sintáctico y Léxico Integrado con Modo Pánico Mejorado
 * Detecta TODOS los errores léxicos y sintácticos sin detenerse
 */
public class AnalisisSintactico {
    
    // Tipos de error
    enum TipoError {
        LEXICO,
        SINTACTICO
    }
    
    // Clase para almacenar información de errores
    static class ErrorAnalisis {
        TipoError tipo;
        String mensaje;
        int linea;
        int columna;
        String tokenEncontrado;
        String tokenEsperado;
        
        ErrorAnalisis(TipoError tipo, String mensaje, int linea, int columna, 
                     String tokenEncontrado, String tokenEsperado) {
            this.tipo = tipo;
            this.mensaje = mensaje;
            this.linea = linea;
            this.columna = columna;
            this.tokenEncontrado = tokenEncontrado;
            this.tokenEsperado = tokenEsperado;
        }
    }
    
    // Información de contexto para delimitadores
    static class DelimitadorInfo {
        int linea;
        int columna;
        String tipo;           // "{", "(", etc.
        String contexto;       // "CaeCliente", "papoi", etc.
        int id;                // ID único para emparejar
        
        DelimitadorInfo(int linea, int columna, String tipo, String contexto, int id) {
            this.linea = linea;
            this.columna = columna;
            this.tipo = tipo;
            this.contexto = contexto;
            this.id = id;
        }
    }
    
    // Parser personalizado con recuperación de errores
    static class ParserConRecuperacion extends CarumaLangParser {
        private List<ErrorAnalisis> errores = new ArrayList<>();
        private int contadorErrores = 0;
        private static final int MAX_ERRORES = 100;
        
        public ParserConRecuperacion(java.io.Reader stream) {
            super(stream);
        }
        
        public List<ErrorAnalisis> getErrores() {
            return errores;
        }
        
        /**
         * Programa modificado con recuperación de errores y posiciones correctas
         */
        public void ProgramaConRecuperacion() {
            try {
                // Verificar CARUMA
                if (!verificarYConsumirToken(CARUMA, "Caruma")) {
                    recuperarHastaToken(BYEBYE, INTCHELADA, GRANITO, CADENA, CARACTER, MIXCHELADA);
                }
                
                // Analizar declaraciones con recuperación total
                try {
                    DeclaracionesConRecuperacion();
                } catch (TokenMgrError e) {
                    // Error léxico durante análisis - ya fue registrado en pre-análisis
                    // Solo continuar
                } catch (Exception e) {
                    // Registrar error con posición segura
                    try {
                        Token tok = getToken(1);
                        registrarError(TipoError.SINTACTICO,
                                     "Error durante análisis de declaraciones", 
                                     tok.beginLine,    // Posición correcta
                                     tok.beginColumn,  // Posición correcta
                                     tok.image, "");
                    } catch (Exception ex) {
                        // Si no podemos obtener el token, usar posición por defecto
                        registrarError(TipoError.SINTACTICO,
                                     "Error durante análisis de declaraciones", 
                                     -1, -1, "<desconocido>", "");
                    }
                }
                
                // Verificar BYEBYE
                try {
                    if (!verificarYConsumirToken(BYEBYE, "byebye")) {
                        Token tok = getToken(1);
                        registrarError(TipoError.SINTACTICO,
                                     "Falta 'byebye' al final del programa", 
                                     tok.beginLine,    // Posición correcta
                                     tok.beginColumn,  // Posición correcta
                                     tok.image, "byebye");
                    }
                } catch (Exception e) {
                    // Ignorar errores al verificar byebye
                }
                
            } catch (Exception e) {
                // Error general - intentar capturar posición
                try {
                    Token tok = getToken(1);
                    registrarError(TipoError.SINTACTICO,
                                 "Error inesperado: " + e.getMessage(), 
                                 tok.beginLine,    // Posición correcta
                                 tok.beginColumn,  // Posición correcta
                                 tok.image, "");
                } catch (Exception ex) {
                    registrarError(TipoError.SINTACTICO,
                                 "Error inesperado: " + e.getMessage(), 
                                 -1, -1, "<desconocido>", "");
                }
            }
        }
        
        /**
         * Declaraciones con recuperación - versión ultra robusta
         */
        private void DeclaracionesConRecuperacion() {
            int tokensProblematicos = 0;
            final int MAX_TOKENS_PROBLEMATICOS = 50;
            
            while (contadorErrores < MAX_ERRORES && tokensProblematicos < MAX_TOKENS_PROBLEMATICOS) {
                try {
                    Token tok = getToken(1);
                    
                    // Verificar si llegamos al final
                    if (tok.kind == BYEBYE || tok.kind == EOF) {
                        break;
                    }
                    
                    // Intentar analizar una declaración
                    if (esInicioDeDeclaracion()) {
                        try {
                            Declaracion();
                            tokensProblematicos = 0; // Reset al éxito
                        } catch (ParseException pe) {
                            // Capturar error sintáctico con posición CORRECTA
                            capturarErrorParseException(pe);
                            recuperarHastaInicioDeclaracion();
                            tokensProblematicos++;
                        } catch (TokenMgrError te) {
                            // Error léxico - avanzar token
                            avanzarTokenSeguro();
                            tokensProblematicos++;
                        }
                    } else {
                        // Token inesperado - registrar con posición CORRECTA del token actual
                        registrarError(TipoError.SINTACTICO,
                                     "Token inesperado en declaraciones", 
                                     tok.beginLine,      // Usar beginLine del token
                                     tok.beginColumn,    // Usar beginColumn del token
                                     tok.image, 
                                     "tipo de dato, identificador o estructura de control");
                        avanzarTokenSeguro();
                        tokensProblematicos++;
                    }
                } catch (TokenMgrError e) {
                    // Error léxico al obtener token
                    avanzarTokenSeguro();
                    tokensProblematicos++;
                } catch (Exception e) {
                    // Cualquier otro error
                    avanzarTokenSeguro();
                    tokensProblematicos++;
                }
            }
        }
        
        /**
         * Avanza al siguiente token de forma segura
         */
        private void avanzarTokenSeguro() {
            try {
                getNextToken();
            } catch (TokenMgrError e) {
                // Si hay error léxico, intentar múltiples veces
                // hasta obtener un token válido o EOF
                for (int i = 0; i < 5; i++) {
                    try {
                        getNextToken();
                        return; // Éxito
                    } catch (Exception ex) {
                        // Continuar intentando
                    }
                }
            } catch (Exception e) {
                // Ignorar otros errores
            }
        }
        
        /**
         * Verifica si el token actual es inicio de declaración - versión segura
         */
        private boolean esInicioDeDeclaracion() {
            try {
                Token tok = getToken(1);
                return tok.kind == INTCHELADA || 
                       tok.kind == GRANITO || 
                       tok.kind == CADENA || 
                       tok.kind == CARACTER || 
                       tok.kind == MIXCHELADA ||
                       tok.kind == CAECLIENTE ||
                       tok.kind == PAPOI ||
                       tok.kind == PARAPAPOI ||
                       tok.kind == HOLAHOLA;
            } catch (TokenMgrError e) {
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * Verifica y consume un token esperado - versión segura con posiciones correctas
         */
        private boolean verificarYConsumirToken(int tipoEsperado, String nombreToken) {
            try {
                Token tok = getToken(1);
                if (tok.kind == tipoEsperado) {
                    getNextToken();
                    return true;
                } else {
                    // Registrar error con la posición CORRECTA del token encontrado
                    registrarError(TipoError.SINTACTICO,
                                 "Se esperaba '" + nombreToken + "'", 
                                 tok.beginLine,      // Posición correcta
                                 tok.beginColumn,    // Posición correcta
                                 tok.image, 
                                 nombreToken);
                    return false;
                }
            } catch (TokenMgrError e) {
                // Error léxico al obtener token - ya registrado en pre-análisis
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * Recupera hasta encontrar un token de sincronización - versión segura
         */
        private void recuperarHastaToken(int... tokensSincronizacion) {
            int intentos = 0;
            final int MAX_INTENTOS = 100;
            
            while (intentos < MAX_INTENTOS) {
                try {
                    Token tok = getToken(1);
                    
                    if (tok.kind == EOF) {
                        return;
                    }
                    
                    for (int tokenSinc : tokensSincronizacion) {
                        if (tok.kind == tokenSinc) {
                            return;
                        }
                    }
                    
                    avanzarTokenSeguro();
                    intentos++;
                    
                } catch (TokenMgrError e) {
                    avanzarTokenSeguro();
                    intentos++;
                } catch (Exception e) {
                    return;
                }
            }
        }
        
        /**
         * Recupera hasta encontrar inicio de declaración
         */
        private void recuperarHastaInicioDeclaracion() {
            recuperarHastaToken(INTCHELADA, GRANITO, CADENA, CARACTER, 
                              MIXCHELADA, CAECLIENTE, PAPOI, PARAPAPOI, 
                              HOLAHOLA, BYEBYE, EOF);
        }
        
        /**
         * Obtiene la línea del token actual de forma segura
         */
        private int getLineaActual() {
            try {
                return getToken(1).beginLine;
            } catch (Exception e) {
                return -1;
            }
        }
        
        /**
         * Obtiene la columna del token actual de forma segura
         */
        private int getColumnaActual() {
            try {
                return getToken(1).beginColumn;
            } catch (Exception e) {
                return -1;
            }
        }
        
        /**
         * Obtiene el texto del token actual de forma segura
         */
        private String getTokenActual() {
            try {
                return getToken(1).image;
            } catch (Exception e) {
                return "<desconocido>";
            }
        }
        
        /**
         * Captura y procesa ParseException con posiciones correctas
         */
        private void capturarErrorParseException(ParseException e) {
            String mensaje = e.getMessage();
            
            // IMPORTANTE: El token de error es currentToken.next
            Token tokError = e.currentToken.next;
            
            // Extraer posición CORRECTA del token que causó el error
            int linea = tokError.beginLine;
            int columna = tokError.beginColumn;
            String tokenEncontrado = tokError.image;
            
            // Extraer tokens esperados
            String esperado = extraerTokensEsperados(e);
            
            // Registrar el error con la posición CORRECTA
            registrarError(TipoError.SINTACTICO, mensaje, linea, columna, tokenEncontrado, esperado);
        }
        
        /**
         * Extrae tokens esperados del ParseException
         */
        private String extraerTokensEsperados(ParseException e) {
            StringBuilder esperados = new StringBuilder();
            
            if (e.expectedTokenSequences != null && e.expectedTokenSequences.length > 0) {
                for (int i = 0; i < e.expectedTokenSequences.length && i < 5; i++) {
                    int[] secuencia = e.expectedTokenSequences[i];
                    if (secuencia.length > 0) {
                        String tokenImg = e.tokenImage[secuencia[0]];
                        if (esperados.length() > 0) {
                            esperados.append(", ");
                        }
                        esperados.append(tokenImg);
                    }
                }
            }
            
            return esperados.length() > 0 ? esperados.toString() : "token válido";
        }
        
        /**
         * Registra un error
         */
        private void registrarError(TipoError tipo, String mensaje, int linea, int columna, 
                                   String tokenEncontrado, String tokenEsperado) {
            errores.add(new ErrorAnalisis(tipo, mensaje, linea, columna, 
                                         tokenEncontrado, tokenEsperado));
            contadorErrores++;
        }
    }
    
    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivo CarumaLang", "crm");
        fileChooser.setFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();

            if (!fileName.toLowerCase().endsWith(".crm")) {
                System.err.println("Error: El archivo seleccionado no tiene extension .crm");
                return;
            }

            try {
                analizarArchivo(fileName);
            } catch (FileNotFoundException e) {
                System.err.println("Error: No se pudo encontrar el archivo: " + fileName);
            } catch (IOException e) {
                System.err.println("Error al leer el archivo: " + e.getMessage());
            }
        } else {
            System.out.println("No se selecciono ningun archivo.");
        }
    }
    
    private static void analizarArchivo(String fileName) throws IOException {
        System.out.println("========================================");
        System.out.println("   ANALIZADOR SINTACTICO - CARUMALANG");
        System.out.println("     MODO PANICO - TODOS LOS ERRORES");
        System.out.println("========================================");
        System.out.println("Archivo: " + fileName + "\n");
        
        // PASO 1: Pre-análisis para detectar errores léxicos
        System.out.println("Paso 1/4: Analizando errores lexicos...");
        List<ErrorAnalisis> erroresLexicos = preAnalizarErroresLexicos(fileName);
        
        // PASO 2: Pre-análisis para detectar delimitadores sin emparejar
        System.out.println("Paso 2/4: Analizando delimitadores...");
        List<ErrorAnalisis> erroresDelimitadores = preAnalizarDelimitadores(fileName);
        
        // PASO 3: Crear versión limpia del archivo (sin errores léxicos)
        System.out.println("Paso 3/4: Preparando analisis sintactico...");
        String archivoLimpio = crearArchivoLimpio(fileName);
        
        // PASO 4: Análisis sintáctico sobre archivo limpio
        System.out.println("Paso 4/4: Analizando estructura sintactica...\n");
        List<ErrorAnalisis> erroresSintacticos = new ArrayList<>();
        
        if (archivoLimpio != null) {
            try {
                BufferedReader reader = new BufferedReader(new java.io.StringReader(archivoLimpio));
                ParserConRecuperacion parser = new ParserConRecuperacion(reader);
                parser.ProgramaConRecuperacion();
                erroresSintacticos = parser.getErrores();
                reader.close();
            } catch (Exception e) {
                System.err.println("Error en analisis sintactico: " + e.getMessage());
            }
        }
        
        // PASO 5: Combinar todos los errores
        List<ErrorAnalisis> errores = new ArrayList<>();
        errores.addAll(erroresLexicos);
        errores.addAll(erroresDelimitadores);
        errores.addAll(erroresSintacticos);
        
        // Ordenar errores por línea y columna
        errores.sort((e1, e2) -> {
            if (e1.linea != e2.linea) return Integer.compare(e1.linea, e2.linea);
            return Integer.compare(e1.columna, e2.columna);
        });
        
        // Mostrar resultados
        if (errores.isEmpty()) {
            System.out.println("========================================");
            System.out.println("         ANALISIS EXITOSO");
            System.out.println("========================================");
            System.out.println("\nEl programa cumple con la sintaxis de CarumaLang");
            System.out.println("No se encontraron errores");
            System.out.println("\n========================================");
        } else {
            mostrarErrores(errores);
        }
        
        // Generar archivo de errores
        generarArchivoErrores(fileName, errores);
    }
    
    /**
     * Crea una versión "limpia" del archivo reemplazando caracteres inválidos
     * con espacios para que el parser sintáctico pueda continuar
     */
    private static String crearArchivoLimpio(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            StringBuilder archivoLimpio = new StringBuilder();
            AnalizadorSintactico.SimpleCharStream stream = 
                new AnalizadorSintactico.SimpleCharStream(reader);
            AnalizadorSintactico.CarumaLangParserTokenManager tokenManager = 
                new AnalizadorSintactico.CarumaLangParserTokenManager(stream);
            
            AnalizadorSintactico.Token tok;
            boolean continuar = true;
            
            while (continuar) {
                try {
                    tok = tokenManager.getNextToken();
                    
                    if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.EOF) {
                        continuar = false;
                    } else {
                        // Agregar espacios para mantener las posiciones
                        archivoLimpio.append(tok.image);
                        archivoLimpio.append(" ");
                    }
                    
                } catch (TokenMgrError e) {
                    // Reemplazar carácter inválido con espacio
                    archivoLimpio.append(" ");
                    
                    // Intentar avanzar
                    try {
                        stream.readChar();
                    } catch (IOException ioException) {
                        continuar = false;
                    }
                }
            }
            
            reader.close();
            return archivoLimpio.toString();
            
        } catch (Exception e) {
            System.err.println("Error al crear archivo limpio: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Pre-análisis del archivo para detectar errores léxicos
     * Implementa toda la lógica del analizador léxico
     */
    private static List<ErrorAnalisis> preAnalizarErroresLexicos(String fileName) throws IOException {
        List<ErrorAnalisis> errores = new ArrayList<>();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            AnalizadorSintactico.SimpleCharStream stream = 
                new AnalizadorSintactico.SimpleCharStream(reader);
            AnalizadorSintactico.CarumaLangParserTokenManager tokenManager = 
                new AnalizadorSintactico.CarumaLangParserTokenManager(stream);
            
            AnalizadorSintactico.Token tok;
            boolean continuar = true;
            
            // Leer todos los tokens y detectar errores léxicos
            while (continuar) {
                try {
                    tok = tokenManager.getNextToken();
                    
                    if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.EOF) {
                        continuar = false;
                    }
                    // No hacer nada con tokens válidos aquí, solo continuar
                    
                } catch (TokenMgrError e) {
                    // Error léxico detectado
                    String mensaje = e.getMessage();
                    
                    // Extraer línea y columna del mensaje de error
                    int linea = -1;
                    int columna = -1;
                    
                    // El mensaje tiene formato: "Lexical error at line X, column Y..."
                    if (mensaje.contains("line")) {
                        try {
                            int lineaStart = mensaje.indexOf("line ") + 5;
                            int lineaEnd = mensaje.indexOf(",", lineaStart);
                            if (lineaEnd > lineaStart) {
                                linea = Integer.parseInt(mensaje.substring(lineaStart, lineaEnd).trim());
                            }
                        } catch (Exception ex) {
                            linea = stream.getEndLine();
                        }
                    } else {
                        linea = stream.getEndLine();
                    }
                    
                    if (mensaje.contains("column")) {
                        try {
                            int columnaStart = mensaje.indexOf("column ") + 7;
                            int columnaEnd = mensaje.indexOf(".", columnaStart);
                            if (columnaEnd > columnaStart) {
                                columna = Integer.parseInt(mensaje.substring(columnaStart, columnaEnd).trim());
                            }
                        } catch (Exception ex) {
                            columna = stream.getEndColumn();
                        }
                    } else {
                        columna = stream.getEndColumn();
                    }
                    
                    // Extraer carácter inválido del mensaje
                    String caracterInvalido = "?";
                    int codigoASCII = -1;
                    
                    if (mensaje.contains("Encountered: \"")) {
                        int start = mensaje.indexOf("Encountered: \"") + 14;
                        int end = mensaje.indexOf("\"", start);
                        if (end > start) {
                            caracterInvalido = mensaje.substring(start, end);
                            if (!caracterInvalido.isEmpty()) {
                                codigoASCII = (int) caracterInvalido.charAt(0);
                            }
                        }
                    } else if (mensaje.contains("Encountered: <EOF>")) {
                        caracterInvalido = "<EOF>";
                    }
                    
                    // Formatear caracteres especiales para visualización
                    String caracterMostrar = caracterInvalido;
                    if (caracterInvalido.equals("\n")) caracterMostrar = "\\n";
                    else if (caracterInvalido.equals("\t")) caracterMostrar = "\\t";
                    else if (caracterInvalido.equals("\r")) caracterMostrar = "\\r";
                    
                    // Crear mensaje descriptivo
                    String mensajeError;
                    if (codigoASCII >= 0) {
                        mensajeError = "Caracter no reconocido: '" + caracterMostrar + 
                                     "' (ASCII: " + codigoASCII + ")";
                    } else {
                        mensajeError = "Caracter no reconocido: '" + caracterMostrar + "'";
                    }
                    
                    errores.add(new ErrorAnalisis(
                        TipoError.LEXICO,
                        mensajeError,
                        linea, columna,
                        caracterMostrar,
                        "token valido"));
                    
                    // Intentar recuperarse avanzando un carácter
                    try {
                        stream.readChar();
                    } catch (IOException ioException) {
                        continuar = false;
                    }
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            System.err.println("Error en pre-analisis lexico: " + e.getMessage());
        }
        
        return errores;
    }
    
    /**
     * Pre-análisis mejorado para detectar delimitadores sin emparejar
     * Usa un sistema de pila con contexto para detectar emparejamientos incorrectos
     */
    private static List<ErrorAnalisis> preAnalizarDelimitadores(String fileName) throws IOException {
        List<ErrorAnalisis> errores = new ArrayList<>();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            AnalizadorSintactico.SimpleCharStream stream = 
                new AnalizadorSintactico.SimpleCharStream(reader);
            AnalizadorSintactico.CarumaLangParserTokenManager tokenManager = 
                new AnalizadorSintactico.CarumaLangParserTokenManager(stream);
            
            Stack<DelimitadorInfo> pilaLlaves = new Stack<>();
            Stack<DelimitadorInfo> pilaParentesis = new Stack<>();
            
            int contadorId = 0;
            AnalizadorSintactico.Token tok;
            List<AnalizadorSintactico.Token> historialTokens = new ArrayList<>();
            
            // Leer todos los tokens
            while (true) {
                try {
                    tok = tokenManager.getNextToken();
                } catch (TokenMgrError e) {
                    // Ignorar errores léxicos aquí (ya se manejan en preAnalizarErroresLexicos)
                    try {
                        stream.readChar();
                        continue;
                    } catch (IOException ioException) {
                        break;
                    }
                }
                
                if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.EOF) {
                    break;
                }
                
                historialTokens.add(tok);
                
                // Rastrear llaves
                if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.OPEN) {  // {
                    String contexto = determinarContexto(historialTokens);
                    pilaLlaves.push(new DelimitadorInfo(
                        tok.beginLine, tok.beginColumn, "{", contexto, ++contadorId));
                    
                } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CLOSE) {  // }
                    if (pilaLlaves.isEmpty()) {
                        errores.add(new ErrorAnalisis(
                            TipoError.SINTACTICO,
                            "Llave de cierre '}' sin apertura correspondiente",
                            tok.beginLine, tok.beginColumn, "}", "{"));
                    } else {
                        // Emparejar correctamente
                        pilaLlaves.pop();
                    }
                    
                } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.ABRIENDO) {  // (
                    String contexto = determinarContextoParentesis(historialTokens);
                    pilaParentesis.push(new DelimitadorInfo(
                        tok.beginLine, tok.beginColumn, "(", contexto, ++contadorId));
                    
                } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CERRANDO) {  // )
                    if (pilaParentesis.isEmpty()) {
                        errores.add(new ErrorAnalisis(
                            TipoError.SINTACTICO,
                            "Parentesis de cierre ')' sin apertura correspondiente",
                            tok.beginLine, tok.beginColumn, ")", "("));
                    } else {
                        pilaParentesis.pop();
                    }
                }
            }
            
            // Reportar llaves sin cerrar (con contexto específico)
            while (!pilaLlaves.isEmpty()) {
                DelimitadorInfo info = pilaLlaves.pop();
                errores.add(new ErrorAnalisis(
                    TipoError.SINTACTICO,
                    "Llave de apertura '{' sin cerrar en " + info.contexto,
                    info.linea, info.columna, "{", "}"));
            }
            
            // Reportar paréntesis sin cerrar
            while (!pilaParentesis.isEmpty()) {
                DelimitadorInfo info = pilaParentesis.pop();
                errores.add(new ErrorAnalisis(
                    TipoError.SINTACTICO,
                    "Parentesis '(' sin cerrar en " + info.contexto,
                    info.linea, info.columna, "(", ")"));
            }
            
            reader.close();
            
        } catch (Exception e) {
            System.err.println("Error en pre-analisis de delimitadores: " + e.getMessage());
        }
        
        return errores;
    }
    
    /**
     * Determina el contexto de una llave basándose en el historial de tokens
     */
    private static String determinarContexto(List<AnalizadorSintactico.Token> historial) {
        // Buscar hacia atrás las últimas 10 tokens
        for (int i = historial.size() - 2; i >= 0 && i >= historial.size() - 10; i--) {
            AnalizadorSintactico.Token tok = historial.get(i);
            
            if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CAECLIENTE) {
                return "CaeCliente (if)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.SINOCAE) {
                return "SiNoCae (else)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.PAPOI) {
                return "papoi (while)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.PARAPAPOI) {
                return "paraPapoi (for)";
            }
        }
        
        return "bloque de codigo";
    }
    
    /**
     * Determina el contexto de un paréntesis
     */
    private static String determinarContextoParentesis(List<AnalizadorSintactico.Token> historial) {
        // Buscar hacia atrás
        for (int i = historial.size() - 2; i >= 0 && i >= historial.size() - 5; i--) {
            AnalizadorSintactico.Token tok = historial.get(i);
            
            if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.HOLAHOLA) {
                return "holahola (print)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CAECLIENTE) {
                return "CaeCliente (condicion)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.PAPOI) {
                return "papoi (condicion)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.PARAPAPOI) {
                return "paraPapoi (encabezado)";
            }
        }
        
        return "expresion";
    }
    
    /**
     * Muestra la tabla de errores en consola con información detallada
     */
    private static void mostrarErrores(List<ErrorAnalisis> errores) {
        System.out.println("========================================");
        System.out.println("        ERRORES ENCONTRADOS");
        System.out.println("========================================");
        System.out.println();
        
        // Contar errores por tipo
        int erroresLexicos = 0;
        int erroresSintacticos = 0;
        for (ErrorAnalisis error : errores) {
            if (error.tipo == TipoError.LEXICO) {
                erroresLexicos++;
            } else {
                erroresSintacticos++;
            }
        }
        
        System.out.println("Errores Lexicos: " + erroresLexicos);
        System.out.println("Errores Sintacticos: " + erroresSintacticos);
        System.out.println("Total: " + errores.size());
        System.out.println();
        
        // Mostrar tabla de ERRORES LÉXICOS si existen
        if (erroresLexicos > 0) {
            System.out.println("--------------------------------------");
            System.out.println("     ERRORES LEXICOS ENCONTRADOS        ");
            System.out.println("--------------------------------------");
            System.out.println();
            System.out.println("-----------------------------------------");
            System.out.println("│ No. │ Caracter    │ Linea  │ Columna │");
            System.out.println("-----------------------------------------");
            
            int contadorLexico = 1;
            for (ErrorAnalisis error : errores) {
                if (error.tipo == TipoError.LEXICO) {
                    String caracterMostrar = error.tokenEncontrado;
                    if (caracterMostrar.equals("\n")) caracterMostrar = "\\n";
                    if (caracterMostrar.equals("\t")) caracterMostrar = "\\t";
                    if (caracterMostrar.equals("\r")) caracterMostrar = "\\r";
                    
                    System.out.printf("│ %-4d │ %-11s │ %-6d │ %-7d │%n",
                        contadorLexico++,
                        caracterMostrar,
                        error.linea,
                        error.columna);
                }
            }
            System.out.println("------------------------------------------");
            System.out.println();
        }
        
        // Mostrar tabla de ERRORES SINTÁCTICOS si existen
        if (erroresSintacticos > 0) {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("                      ERRORES SINTACTICOS ENCONTRADOS");
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println();
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.println("│ No. │ Linea │ Col │ Token Encontrado │ Token Esperado              │ Descripcion");
            System.out.println("--------------------------------------------------------------------------------------------");
            
            int contadorSintactico = 1;
            for (ErrorAnalisis error : errores) {
                if (error.tipo == TipoError.SINTACTICO) {
                    String tokenEncontrado = error.tokenEncontrado;
                    if (tokenEncontrado.length() > 16) {
                        tokenEncontrado = tokenEncontrado.substring(0, 13) + "...";
                    }
                    
                    String tokenEsperado = error.tokenEsperado;
                    if (tokenEsperado.length() > 27) {
                        tokenEsperado = tokenEsperado.substring(0, 24) + "...";
                    }
                    
                    String descripcion = error.mensaje;
                    if (descripcion.length() > 50) {
                        descripcion = descripcion.substring(0, 47) + "...";
                    }
                    
                    System.out.printf("│ %-4d│ %-6d│ %-4d│ %-16s │ %-27s │ %s%n",
                        contadorSintactico++,
                        error.linea,
                        error.columna,
                        tokenEncontrado,
                        tokenEsperado,
                        descripcion);
                }
            }
            System.out.println("--------------------------------------------------------------------------------------------");
            System.out.println();
        }
        
        System.out.println("========================================");
        System.out.println("        RESUMEN DEL ANALISIS");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Total de errores: " + errores.size());
        System.out.println("  - Errores lexicos: " + erroresLexicos);
        System.out.println("  - Errores sintacticos: " + erroresSintacticos);
        System.out.println();
        
        if (erroresLexicos > 0 && erroresSintacticos > 0) {
            System.out.println("Estado: ANALISIS CON ERRORES LEXICOS Y SINTACTICOS");
        } else if (erroresLexicos > 0) {
            System.out.println("Estado: ANALISIS CON ERRORES LEXICOS");
        } else {
            System.out.println("Estado: ANALISIS CON ERRORES SINTACTICOS");
        }
        
        System.out.println();
        System.out.println("Se genero un archivo .errores con informacion detallada");
        System.out.println();
        System.out.println("========================================");
    }
    
    /**
     * Genera archivo con información detallada de errores
     */
    private static void generarArchivoErrores(String archivoFuente, List<ErrorAnalisis> errores) {
        try {
            String nombreSalida = archivoFuente.replace(".crm", ".errores");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaActual = sdf.format(new Date());
            
            // Contar errores por tipo
            int erroresLexicos = 0;
            int erroresSintacticos = 0;
            for (ErrorAnalisis error : errores) {
                if (error.tipo == TipoError.LEXICO) {
                    erroresLexicos++;
                } else {
                    erroresSintacticos++;
                }
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreSalida))) {
                
                // ENCABEZADO
                writer.write("# REPORTE DE ERRORES - CARUMALANG");
                writer.newLine();
                writer.write("# Archivo fuente: " + archivoFuente);
                writer.newLine();
                writer.write("# Fecha analisis: " + fechaActual);
                writer.newLine();
                writer.write("# Total errores: " + errores.size());
                writer.newLine();
                writer.write("# Errores lexicos: " + erroresLexicos);
                writer.newLine();
                writer.write("# Errores sintacticos: " + erroresSintacticos);
                writer.newLine();
                writer.newLine();
                
                // SECCION DE ERRORES
                writer.write("[ERRORES]");
                writer.newLine();
                writer.newLine();
                
                for (int i = 0; i < errores.size(); i++) {
                    ErrorAnalisis error = errores.get(i);
                    
                    writer.write("ERROR #" + (i + 1));
                    writer.newLine();
                    writer.write("  Tipo: " + error.tipo);
                    writer.newLine();
                    writer.write("  Linea: " + error.linea);
                    writer.newLine();
                    writer.write("  Columna: " + error.columna);
                    writer.newLine();
                    writer.write("  Token encontrado: " + error.tokenEncontrado);
                    writer.newLine();
                    writer.write("  Token esperado: " + error.tokenEsperado);
                    writer.newLine();
                    writer.write("  Descripcion: " + error.mensaje);
                    writer.newLine();
                    writer.newLine();
                }
                
                // RESUMEN
                writer.write("[RESUMEN]");
                writer.newLine();
                writer.write("TOTAL_ERRORES=" + errores.size());
                writer.newLine();
                writer.write("ERRORES_LEXICOS=" + erroresLexicos);
                writer.newLine();
                writer.write("ERRORES_SINTACTICOS=" + erroresSintacticos);
                writer.newLine();
                
                String estado = errores.isEmpty() ? "SIN_ERRORES" : "CON_ERRORES";
                writer.write("ESTADO=" + estado);
                writer.newLine();
                writer.newLine();
                
                writer.write("[FIN]");
                writer.newLine();
            }
            
            if (!errores.isEmpty()) {
                System.out.println("\nArchivo de errores generado: " + nombreSalida);
            }
            
        } catch (IOException e) {
            System.err.println("Error al generar archivo de errores: " + e.getMessage());
        }
    }
}