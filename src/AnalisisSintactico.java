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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import AnalizadorSintactico.*;

/**
 * Analizador Sintáctico con Modo Pánico
 * Detecta TODOS los errores sintácticos sin detenerse
 */
public class AnalisisSintactico {
    
    // Clase para almacenar información de errores sintácticos
    static class ErrorSintactico {
        String mensaje;
        int linea;
        int columna;
        String tokenEncontrado;
        String tokenEsperado;
        
        ErrorSintactico(String mensaje, int linea, int columna, String tokenEncontrado, String tokenEsperado) {
            this.mensaje = mensaje;
            this.linea = linea;
            this.columna = columna;
            this.tokenEncontrado = tokenEncontrado;
            this.tokenEsperado = tokenEsperado;
        }
    }
    
    // Parser personalizado con recuperación de errores
    static class ParserConRecuperacion extends CarumaLangParser {
        private List<ErrorSintactico> errores = new ArrayList<>();
        private int contadorErrores = 0;
        private static final int MAX_ERRORES = 50;
        
        public ParserConRecuperacion(java.io.Reader stream) {
            super(stream);
        }
        
        public List<ErrorSintactico> getErrores() {
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
                    registrarError("Falta 'byebye' al final del programa", 
                                 getLineaActual(), getColumnaActual(), 
                                 getTokenActual(), "byebye");
                }
                
            } catch (Exception e) {
                registrarError("Error inesperado: " + e.getMessage(), 
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
                        registrarError("Token inesperado en declaraciones", 
                                     tok.beginLine, tok.beginColumn,
                                     tok.image, "tipo de dato, identificador o estructura de control");
                        getNextToken();
                    }
                } catch (ParseException e) {
                    // Capturar error y continuar
                    capturarErrorParseException(e);
                    recuperarHastaInicioDeclaracion();
                } catch (TokenMgrError e) {
                    registrarError("Error léxico: " + e.getMessage(), 
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
                registrarError("Se esperaba '" + nombreToken + "'", 
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
            
            registrarError(mensaje, linea, columna, tokenEncontrado, esperado);
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
         * Registra un error sintáctico
         */
        private void registrarError(String mensaje, int linea, int columna, 
                                   String tokenEncontrado, String tokenEsperado) {
            errores.add(new ErrorSintactico(mensaje, linea, columna, 
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
        
        // PASO 1: Pre-análisis para detectar llaves sin cerrar
        List<ErrorSintactico> erroresLlaves = preAnalizarLlaves(fileName);
        
        // PASO 2: Análisis sintáctico normal
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        ParserConRecuperacion parser = new ParserConRecuperacion(reader);
        
        System.out.println("Iniciando analisis sintactico en modo panico...\n");
        
        // Ejecutar análisis con recuperación de errores
        parser.ProgramaConRecuperacion();
        
        List<ErrorSintactico> errores = parser.getErrores();
        
        // PASO 3: Combinar errores de llaves con errores sintácticos
        errores.addAll(erroresLlaves);
        
        // Mostrar resultados
        if (errores.isEmpty()) {
            System.out.println("========================================");
            System.out.println("         ANALISIS EXITOSO");
            System.out.println("========================================");
            System.out.println("\nEl programa cumple con la sintaxis de CarumaLang");
            System.out.println("No se encontraron errores sintacticos");
            System.out.println("\n========================================");
        } else {
            mostrarErrores(errores);
        }
        
        // Generar archivo de errores
        generarArchivoErrores(fileName, errores);
        
        reader.close();
    }
    
    /**
     * Pre-análisis del archivo para detectar llaves sin cerrar
     * Lee el archivo token por token y verifica el balance de delimitadores
     */
    private static List<ErrorSintactico> preAnalizarLlaves(String fileName) throws IOException {
        List<ErrorSintactico> errores = new ArrayList<>();
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            AnalizadorSintactico.SimpleCharStream stream = 
                new AnalizadorSintactico.SimpleCharStream(reader);
            AnalizadorSintactico.CarumaLangParserTokenManager tokenManager = 
                new AnalizadorSintactico.CarumaLangParserTokenManager(stream);
            
            List<InfoLlave> pilaLlaves = new ArrayList<>();
            List<InfoLlave> pilaParentesis = new ArrayList<>();
            
            AnalizadorSintactico.Token tok;
            AnalizadorSintactico.Token tokenPrevio = null;
            
            // Leer todos los tokens
            while (true) {
                tok = tokenManager.getNextToken();
                
                if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.EOF) {
                    break;
                }
                
                // Rastrear llaves
                if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.OPEN) {  // {
                    String contexto = determinarContexto(tokenPrevio);
                    pilaLlaves.add(new InfoLlave(tok.beginLine, tok.beginColumn, contexto, "{"));
                } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CLOSE) {  // }
                    if (pilaLlaves.isEmpty()) {
                        errores.add(new ErrorSintactico(
                            "Llave de cierre '}' sin llave de apertura correspondiente",
                            tok.beginLine, tok.beginColumn, "}", "{"));
                    } else {
                        pilaLlaves.remove(pilaLlaves.size() - 1);
                    }
                } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.ABRIENDO) {  // (
                    pilaParentesis.add(new InfoLlave(tok.beginLine, tok.beginColumn, "expresión", "("));
                } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CERRANDO) {  // )
                    if (!pilaParentesis.isEmpty()) {
                        pilaParentesis.remove(pilaParentesis.size() - 1);
                    }
                }
                
                tokenPrevio = tok;
            }
            
            // Reportar llaves sin cerrar
            for (InfoLlave info : pilaLlaves) {
                errores.add(new ErrorSintactico(
                    "Llave de apertura '{' sin cerrar en " + info.contexto,
                    info.linea, info.columna, "{", "}"));
            }
            
            // Reportar paréntesis sin cerrar
            for (InfoLlave info : pilaParentesis) {
                errores.add(new ErrorSintactico(
                    "Paréntesis de apertura '(' sin cerrar",
                    info.linea, info.columna, "(", ")"));
            }
            
            reader.close();
            
        } catch (Exception e) {
            System.err.println("Error en pre-análisis de llaves: " + e.getMessage());
        }
        
        return errores;
    }
    
    /**
     * Determina el contexto basado en el token previo
     */
    private static String determinarContexto(AnalizadorSintactico.Token tokenPrevio) {
        if (tokenPrevio == null) {
            return "bloque de código";
        }
        
        // Retroceder hasta 10 tokens para encontrar palabra clave
        AnalizadorSintactico.Token tok = tokenPrevio;
        for (int i = 0; i < 10 && tok != null; i++) {
            if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.CAECLIENTE) {
                return "CaeCliente (if)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.SINOCAE) {
                return "SiNoCae (else)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.PAPOI) {
                return "papoi (while)";
            } else if (tok.kind == AnalizadorSintactico.CarumaLangParserConstants.PARAPAPOI) {
                return "paraPapoi (for)";
            }
            
            // No podemos retroceder más sin estructura compleja
            break;
        }
        
        return "bloque de código";
    }
    
    /**
     * Clase auxiliar para información de llaves en pre-análisis
     */
    static class InfoLlave {
        int linea;
        int columna;
        String contexto;
        String tipo;
        
        InfoLlave(int linea, int columna, String contexto, String tipo) {
            this.linea = linea;
            this.columna = columna;
            this.contexto = contexto;
            this.tipo = tipo;
        }
    }
    
    /**
     * Muestra la tabla de errores en consola
     */
    private static void mostrarErrores(List<ErrorSintactico> errores) {
        System.out.println("========================================");
        System.out.println("     ERRORES SINTACTICOS ENCONTRADOS");
        System.out.println("========================================");
        System.out.println();
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println("│ No. │ Línea │ Col │ Encontrado      │ Esperado              │ Descripción");
        System.out.println("----------------------------------------------------------------------------------------");
        
        for (int i = 0; i < errores.size(); i++) {
            ErrorSintactico error = errores.get(i);
            
            String tokenEncontrado = error.tokenEncontrado;
            if (tokenEncontrado.length() > 15) {
                tokenEncontrado = tokenEncontrado.substring(0, 12) + "...";
            }
            
            String tokenEsperado = error.tokenEsperado;
            if (tokenEsperado.length() > 20) {
                tokenEsperado = tokenEsperado.substring(0, 17) + "...";
            }
            
            // Extraer descripción corta del mensaje
            String descripcion = extraerDescripcionCorta(error.mensaje);
            if (descripcion.length() > 40) {
                descripcion = descripcion.substring(0, 37) + "...";
            }
            
            System.out.printf("│ %-4d│ %-6d│ %-4d│ %-15s │ %-21s │ %s%n",
                i + 1,
                error.linea,
                error.columna,
                tokenEncontrado,
                tokenEsperado,
                descripcion);
        }
        
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.println();
        
        System.out.println("========================================");
        System.out.println("        RESUMEN DEL ANALISIS");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Total de errores sintacticos: " + errores.size());
        System.out.println("Estado: ANALISIS COMPLETADO CON ERRORES");
        System.out.println();
        System.out.println("Revise la tabla de errores para mas detalles");
        System.out.println("Se genero un archivo .errores con informacion detallada");
        System.out.println();
        System.out.println("========================================");
    }
    
    /**
     * Extrae una descripción corta del mensaje de error
     */
    private static String extraerDescripcionCorta(String mensaje) {
        if (mensaje.contains("Encountered")) {
            return "Token inesperado";
        } else if (mensaje.contains("esperaba")) {
            return "Token esperado no encontrado";
        } else if (mensaje.contains("Falta")) {
            return mensaje.split("\\.")[0];
        } else {
            // Tomar primeras palabras
            String[] palabras = mensaje.split(" ");
            StringBuilder corto = new StringBuilder();
            for (int i = 0; i < Math.min(5, palabras.length); i++) {
                if (i > 0) corto.append(" ");
                corto.append(palabras[i]);
            }
            return corto.toString();
        }
    }
    
    /**
     * Genera archivo con información detallada de errores
     */
    private static void generarArchivoErrores(String archivoFuente, List<ErrorSintactico> errores) {
        try {
            String nombreSalida = archivoFuente.replace(".crm", ".errores");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaActual = sdf.format(new Date());
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreSalida))) {
                
                // ENCABEZADO
                writer.write("# REPORTE DE ERRORES SINTACTICOS - CARUMALANG");
                writer.newLine();
                writer.write("# Archivo fuente: " + archivoFuente);
                writer.newLine();
                writer.write("# Fecha analisis: " + fechaActual);
                writer.newLine();
                writer.write("# Total errores: " + errores.size());
                writer.newLine();
                writer.newLine();
                
                // SECCION DE ERRORES
                writer.write("[ERRORES_SINTACTICOS]");
                writer.newLine();
                writer.newLine();
                
                for (int i = 0; i < errores.size(); i++) {
                    ErrorSintactico error = errores.get(i);
                    
                    writer.write("ERROR #" + (i + 1));
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