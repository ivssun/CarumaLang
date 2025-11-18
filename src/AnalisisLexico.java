import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import AnalizadorLexico.*;

public class AnalisisLexico {
    
    // Clase para almacenar información de errores
    static class ErrorLexico {
        String mensaje;
        int linea;
        int columna;
        String caracterInvalido;
        
        ErrorLexico(String mensaje, int linea, int columna, String caracterInvalido) {
            this.mensaje = mensaje;
            this.linea = linea;
            this.columna = columna;
            this.caracterInvalido = caracterInvalido;
        }
    }

    // Clase para representar un elemento (token o error) con su posición
    static class ElementoAnalisis {
        boolean esError;
        int numero;
        String lexema;
        String tipoToken;
        int linea;
        int columna;
        String mensajeError;
        
        // Constructor para token válido
        ElementoAnalisis(int numero, Token token) {
            this.esError = false;
            this.numero = numero;
            this.lexema = token.image;
            this.tipoToken = CarumaLangLexerConstants.tokenImage[token.kind];
            this.linea = token.beginLine;
            this.columna = token.beginColumn;
            this.mensajeError = "";
        }
        
        // Constructor para error
        ElementoAnalisis(ErrorLexico error) {
            this.esError = true;
            this.numero = -1;
            this.lexema = error.caracterInvalido;
            this.tipoToken = "INVALID";
            this.linea = error.linea;
            this.columna = error.columna;
            this.mensajeError = error.mensaje;
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
                System.err.println("Error: El archivo seleccionado, no tiene una extension .crm");
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
            System.out.println("No se seleccionó ningún archivo.");
        }
    }
    
    private static void analizarArchivo(String fileName) throws IOException {
        System.out.println("========================================");
        System.out.println("   ANALIZADOR LÉXICO - CARUMALANG");
        System.out.println("========================================");
        System.out.println("Archivo: " + fileName + "\n");
        
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        SimpleCharStream stream = new SimpleCharStream(reader);
        CarumaLangLexerTokenManager lexer = new CarumaLangLexerTokenManager(stream);
        
        List<Token> tokensValidos = new ArrayList<>();
        List<ErrorLexico> errores = new ArrayList<>();
        
        //System.out.println("TOKENS RECONOCIDOS:");
        //System.out.println("--------------------------------------------------------------------------------------------------");
        
        boolean continuar = true;
        while (continuar) {
            try {
                Token token = lexer.getNextToken();
                
                if (token.kind == CarumaLangLexerConstants.EOF) {
                    continuar = false;
                } else if (token.kind == CarumaLangLexerConstants.INVALID) {
                    // Token INVALID reconocido - tratarlo como error pero continuar
                    String caracterInvalido = token.image;
                    String mensaje = "Carácter no reconocido: '" + caracterInvalido + 
                                   "' (ASCII: " + (int)caracterInvalido.charAt(0) + ")";
                    
                    errores.add(new ErrorLexico(mensaje, token.beginLine, token.beginColumn, caracterInvalido));
                    
                    /*System.out.printf("ERROR | %-35s | Carácter inválido              | Línea: %d, Col: %d%n",
                        caracterInvalido,
                        token.beginLine,
                        token.beginColumn);*/
                } else {
                    tokensValidos.add(token);
                    String tokenName = CarumaLangLexerConstants.tokenImage[token.kind];
                    /*System.out.printf("%-5d | %-35s | %-30s | Línea: %d, Col: %d%n", 
                        tokensValidos.size(),
                        token.image,
                        tokenName,
                        token.beginLine,
                        token.beginColumn);*/
                }
            } catch (TokenMgrError e) {
                // Capturar información del error (backup por si el token INVALID falla)
                String mensaje = e.getMessage();
                int linea = stream.getEndLine();
                int columna = stream.getEndColumn();
                
                // Extraer el carácter problemático del mensaje de error
                String caracterInvalido = "?";
                if (mensaje.contains("Encountered: \"")) {
                    int start = mensaje.indexOf("Encountered: \"") + 14;
                    int end = mensaje.indexOf("\"", start);
                    if (end > start) {
                        caracterInvalido = mensaje.substring(start, end);
                    }
                }
                
                errores.add(new ErrorLexico(mensaje, linea, columna, caracterInvalido));
                
                //System.out.printf("ERROR | %-20s | Error léxico            | Línea: %d, Col: %d%n", caracterInvalido, linea, columna);
                
                // Intentar recuperarse: avanzar un carácter
                try {
                    stream.readChar();
                } catch (IOException ioException) {
                    continuar = false;
                }
            }
        }
        
        System.out.println("---------------------------------------------------------------------------------------------------");
        
        // Mostrar tabla de errores si los hay
        if (!errores.isEmpty()) {
            /*System.out.println("\n--------------------------------------");
            System.out.println("     ERRORES LÉXICOS ENCONTRADOS        ");
            System.out.println("--------------------------------------");
            System.out.println();
            System.out.println("-----------------------------------------");
            System.out.println("│ No. │ Carácter    │ Línea  │ Columna │");
            System.out.println("-----------------------------------------");*/
            
            for (int i = 0; i < errores.size(); i++) {
                ErrorLexico error = errores.get(i);
                String caracterMostrar = error.caracterInvalido;
                if (caracterMostrar.equals("\n")) caracterMostrar = "\\n";
                if (caracterMostrar.equals("\t")) caracterMostrar = "\\t";
                if (caracterMostrar.equals("\r")) caracterMostrar = "\\r";
                
                /*System.out.printf("│ %-4d │ %-11s │ %-6d │ %-7d │%n", 
                    i + 1,
                    caracterMostrar,
                    error.linea,
                    error.columna);*/
            }
            //System.out.println("------------------------------------------");
        }
        
        // Resumen final
        System.out.println("\n--------------------------------------");
        System.out.println("        RESUMEN DEL ANÁLISIS            ");
        System.out.println("--------------------------------------");
        System.out.println();
        System.out.println("Tokens válidos reconocidos: " + tokensValidos.size());
        System.out.println("Errores léxicos encontrados: " + errores.size());
        System.out.println();
        
        if (errores.isEmpty()) {
            System.out.println("Análisis léxico completado SIN ERRORES");
            System.out.println("El archivo cumple con la sintaxis léxica de CarumaLang");
        } else {
            System.out.println("Análisis completado CON ERRORES");
            System.out.println("Se encontraron " + errores.size() + " caracteres no reconocidos");
            System.out.println("Revise la tabla de errores para más detalles");
        }
        
        System.out.println("\n========================================");

        // Generar archivo de tokens
        generarArchivoTokens(fileName, tokensValidos, errores);
        
        reader.close();
    }

    /**
     * Genera un archivo .tokens con la información del análisis léxico
     * 
     * @param archivoFuente Ruta del archivo .crm analizado
     * @param tokensValidos Lista de tokens válidos encontrados
     * @param errores Lista de errores léxicos encontrados
     */
    private static void generarArchivoTokens(String archivoFuente, 
                                             List<Token> tokensValidos, 
                                             List<ErrorLexico> errores) {
        try {
            // 1. Crear nombre del archivo de salida
            String nombreSalida = archivoFuente.replace(".crm", ".tokens");
            
            // 2. Obtener fecha y hora actual
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaActual = sdf.format(new Date());
            
            // 3. Crear lista unificada de elementos (tokens + errores) ordenada
            List<ElementoAnalisis> elementos = new ArrayList<>();
            
            // Agregar tokens válidos
            for (int i = 0; i < tokensValidos.size(); i++) {
                elementos.add(new ElementoAnalisis(i + 1, tokensValidos.get(i)));
            }
            
            // Agregar errores
            for (ErrorLexico error : errores) {
                elementos.add(new ElementoAnalisis(error));
            }
            
            // Ordenar por línea y columna
            Collections.sort(elementos, new Comparator<ElementoAnalisis>() {
                @Override
                public int compare(ElementoAnalisis e1, ElementoAnalisis e2) {
                    if (e1.linea != e2.linea) {
                        return Integer.compare(e1.linea, e2.linea);
                    }
                    return Integer.compare(e1.columna, e2.columna);
                }
            });
            
            // 4. Escribir archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreSalida))) {
                
                // ===== ENCABEZADO =====
                writer.write("# ARCHIVO DE TOKENS - CARUMALANG");
                writer.newLine();
                writer.write("# Archivo fuente: " + archivoFuente);
                writer.newLine();
                writer.write("# Fecha generacion: " + fechaActual);
                writer.newLine();
                writer.write("# Tokens validos: " + tokensValidos.size());
                writer.newLine();
                writer.write("# Errores lexicos: " + errores.size());
                writer.newLine();
                writer.newLine();
                
                // ===== SECCION DE TOKENS =====
                writer.write("[TOKENS]");
                writer.newLine();
                
                // Contador para tokens válidos (para mantener numeración correcta)
                int contadorTokens = 1;
                
                for (ElementoAnalisis elem : elementos) {
                    if (elem.esError) {
                        // Escribir error
                        writer.write(String.format("ERROR|%s|%s|%d|%d|%s",
                            elem.lexema,
                            elem.tipoToken,
                            elem.linea,
                            elem.columna,
                            elem.mensajeError));
                    } else {
                        // Escribir token válido
                        writer.write(String.format("%d|%s|%s|%d|%d|VALIDO",
                            contadorTokens++,
                            elem.lexema,
                            elem.tipoToken,
                            elem.linea,
                            elem.columna));
                    }
                    writer.newLine();
                }
                
                writer.newLine();
                
                // ===== SECCION DE RESUMEN =====
                writer.write("[RESUMEN]");
                writer.newLine();
                writer.write("TOKENS_VALIDOS=" + tokensValidos.size());
                writer.newLine();
                writer.write("ERRORES_LEXICOS=" + errores.size());
                writer.newLine();
                
                String estado = errores.isEmpty() ? "SIN_ERRORES" : "CON_ERRORES";
                writer.write("ESTADO=" + estado);
                writer.newLine();
                writer.newLine();
                
                // ===== FIN =====
                writer.write("[FIN]");
                writer.newLine();
                
            }
            
            System.out.println("\nArchivo de tokens generado exitosamente: " + nombreSalida);
            
        } catch (IOException e) {
            System.err.println("Error al generar archivo de tokens: " + e.getMessage());
            System.err.println("El analisis lexico se completo, pero no se pudo guardar el archivo.");
        }
    }
    
}