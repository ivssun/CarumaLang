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
 * Analizador Sintáctico con Modo Pánico Mejorado
 * Detecta TODOS los errores sintácticos y léxicos sin detenerse
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
         * Programa modificado con recuperación de errores
         */
        public void ProgramaConRecuperacion() {
            try {
                // Verificar CARUMA
                if (!verificarYConsumirToken(CARUMA, "Caruma")) {
                    recuperarHastaToken(BYEBYE, INTCHELADA, GRANITO, CADENA, CARACTER, MIXCHELADA);
                }
                
                // Analizar declaraciones con recuperación
                DeclaracionesConRecuperacion();
                
                // Verificar BYEBYE
                if (!verificarYConsumirToken(BYEBYE, "byebye")) {
                    registrarError(TipoError.SINTACTICO,
                                 "Falta 'byebye' al final del programa", 
                                 getLineaActual(), getColumnaActual(), 
                                 getTokenActual(), "byebye");
                }
                
            } catch (Exception e) {
                registrarError(TipoError.SINTACTICO,
                             "Error inesperado: " + e.getMessage(), 
                             getLineaActual(), getColumnaActual(), 
                             getTokenActual(), "");
            }
        }
        
        /**
         * Declaraciones con recuperación
         */
        private void DeclaracionesConRecuperacion() {
            Token tok = getToken(1);
            
            while (tok.kind != BYEBYE && tok.kind != EOF && contadorErrores < MAX_ERRORES) {
                try {
                    // Intentar analizar una declaración
                    if (esInicioDeDeclaracion()) {
                        Declaracion();
                    } else {
                        // Token inesperado - registrar error y avanzar
                        registrarError(TipoError.SINTACTICO,
                                     "Token inesperado en declaraciones", 
                                     tok.beginLine, tok.beginColumn,
                                     tok.image, "tipo de dato, identificador o estructura de control");
                        getNextToken();
                    }
                } catch (ParseException e) {
                    // Capturar error y continuar
                    capturarErrorParseException(e);
                    recuperarHastaInicioDeclaracion();
                } catch (TokenMgrError e) {
                    registrarError(TipoError.LEXICO,
                                 "Error léxico: " + e.getMessage(), 
                                 tok.beginLine, tok.beginColumn,
                                 tok.image, "");
                    getNextToken();
                }
                
                tok = getToken(1);
            }
        }
        
        /**
         * Verifica si el token actual es inicio de declaración
         */
        private boolean esInicioDeDeclaracion() {
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
        }
        
        /**
         * Verifica y consume un token esperado
         */
        private boolean verificarYConsumirToken(int tipoEsperado, String nombreToken) {
            Token tok = getToken(1);
            if (tok.kind == tipoEsperado) {
                getNextToken();
                return true;
            } else {
                registrarError(TipoError.SINTACTICO,
                             "Se esperaba '" + nombreToken + "'", 
                             tok.beginLine, tok.beginColumn,
                             tok.image, nombreToken);
                return false;
            }
        }
        
        /**
         * Recupera hasta encontrar un token de sincronización
         */
        private void recuperarHastaToken(int... tokensSincronizacion) {
            Token tok = getToken(1);
            
            while (tok.kind != EOF && contadorErrores < MAX_ERRORES) {
                for (int tokenSinc : tokensSincronizacion) {
                    if (tok.kind == tokenSinc) {
                        return;
                    }
                }
                getNextToken();
                tok = getToken(1);
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
         * Obtiene la línea del token actual
         */
        private int getLineaActual() {
            return getToken(1).beginLine;
        }
        
        /**
         * Obtiene la columna del token actual
         */
        private int getColumnaActual() {
            return getToken(1).beginColumn;
        }
        
        /**
         * Obtiene el texto del token actual
         */
        private String getTokenActual() {
            return getToken(1).image;
        }
        
        /**
         * Captura y procesa ParseException
         */
        private void capturarErrorParseException(ParseException e) {
            String mensaje = e.getMessage();
            Token tokError = e.currentToken.next;
            int linea = tokError.beginLine;
            int columna = tokError.beginColumn;
            String tokenEncontrado = tokError.image;
            
            // Extraer tokens esperados
            String esperado = extraerTokensEsperados(e);
            
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
        List<ErrorAnalisis> erroresLexicos = preAnalizarErroresLexicos(fileName);
        
        // PASO 2: Pre-análisis para detectar delimitadores sin emparejar
        List<ErrorAnalisis> erroresDelimitadores = preAnalizarDelimitadores(fileName);
        
        // PASO 3: Análisis sintáctico normal
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        ParserConRecuperacion parser = new ParserConRecuperacion(reader);
        
        System.out.println("Iniciando analisis sintactico en modo panico...\n");
        
        // Ejecutar análisis con recuperación de errores
        parser.ProgramaConRecuperacion();
        
        List<ErrorAnalisis> errores = parser.getErrores();
        
        // PASO 4: Combinar todos los errores
        errores.addAll(erroresLexicos);
        errores.addAll(erroresDelimitadores);
        
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
        
        reader.close();
    }
    
    /**
     * Pre-análisis del archivo para detectar errores léxicos
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
            
            // Leer todos los tokens y detectar errores léxicos
            while (true) {
                try {
                    tok = tokenManager.getNextToken();
                    
                    if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.EOF) {
                        break;
                    }
                    
                } catch (TokenMgrError e) {
                    // Error léxico detectado
                    String mensaje = e.getMessage();
                    int linea = stream.getEndLine();
                    int columna = stream.getEndColumn();
                    
                    // Extraer carácter inválido
                    String caracterInvalido = "?";
                    if (mensaje.contains("Encountered: \"")) {
                        int start = mensaje.indexOf("Encountered: \"") + 14;
                        int end = mensaje.indexOf("\"", start);
                        if (end > start) {
                            caracterInvalido = mensaje.substring(start, end);
                        }
                    }
                    
                    errores.add(new ErrorAnalisis(
                        TipoError.LEXICO,
                        "Caracter no reconocido",
                        linea, columna,
                        caracterInvalido,
                        "token valido"));
                    
                    // Intentar recuperarse avanzando un carácter
                    try {
                        stream.readChar();
                    } catch (IOException ioException) {
                        break;
                    }
                }
            }
            
            reader.close();
            
        } catch (Exception e) {
            System.err.println("Error en pre-análisis léxico: " + e.getMessage());
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
            System.err.println("Error en pre-análisis de delimitadores: " + e.getMessage());
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
     * Muestra la tabla de errores en consola (SIN columna DESCRIPCION)
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
        
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("│ No. │ Tipo       │ Linea │ Col │ Encontrado      │ Esperado");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (int i = 0; i < errores.size(); i++) {
            ErrorAnalisis error = errores.get(i);
            
            String tipoStr = error.tipo == TipoError.LEXICO ? "LEXICO" : "SINTACTICO";
            
            String tokenEncontrado = error.tokenEncontrado;
            if (tokenEncontrado.length() > 15) {
                tokenEncontrado = tokenEncontrado.substring(0, 12) + "...";
            }
            
            String tokenEsperado = error.tokenEsperado;
            if (tokenEsperado.length() > 30) {
                tokenEsperado = tokenEsperado.substring(0, 27) + "...";
            }
            
            System.out.printf("│ %-4d│ %-10s │ %-6d│ %-4d│ %-15s │ %s%n",
                i + 1,
                tipoStr,
                error.linea,
                error.columna,
                tokenEncontrado,
                tokenEsperado);
        }
        
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println();
        
        System.out.println("========================================");
        System.out.println("        RESUMEN DEL ANALISIS");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Total de errores: " + errores.size());
        System.out.println("  - Errores lexicos: " + erroresLexicos);
        System.out.println("  - Errores sintacticos: " + erroresSintacticos);
        System.out.println();
        System.out.println("Estado: ANALISIS COMPLETADO CON ERRORES");
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