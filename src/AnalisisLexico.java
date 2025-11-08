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
        
        System.out.println("TOKENS RECONOCIDOS:");
        System.out.println("--------------------------------------------------------------------------------------------------");
        
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
                    
                    System.out.printf("ERROR | %-35s | Carácter inválido              | Línea: %d, Col: %d%n",
                        caracterInvalido,
                        token.beginLine,
                        token.beginColumn);
                } else {
                    tokensValidos.add(token);
                    String tokenName = CarumaLangLexerConstants.tokenImage[token.kind];
                    System.out.printf("%-5d | %-35s | %-30s | Línea: %d, Col: %d%n", 
                        tokensValidos.size(),
                        token.image,
                        tokenName,
                        token.beginLine,
                        token.beginColumn);
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
                
                System.out.printf("ERROR | %-20s | Error léxico            | Línea: %d, Col: %d%n", caracterInvalido, linea, columna);
                
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
            System.out.println("\n--------------------------------------");
            System.out.println("     ERRORES LÉXICOS ENCONTRADOS        ");
            System.out.println("--------------------------------------");
            System.out.println();
            System.out.println("-----------------------------------------");
            System.out.println("│ No. │ Carácter    │ Línea  │ Columna │");
            System.out.println("-----------------------------------------");
            
            for (int i = 0; i < errores.size(); i++) {
                ErrorLexico error = errores.get(i);
                String caracterMostrar = error.caracterInvalido;
                if (caracterMostrar.equals("\n")) caracterMostrar = "\\n";
                if (caracterMostrar.equals("\t")) caracterMostrar = "\\t";
                if (caracterMostrar.equals("\r")) caracterMostrar = "\\r";
                
                System.out.printf("│ %-4d │ %-11s │ %-6d │ %-7d │%n", 
                    i + 1,
                    caracterMostrar,
                    error.linea,
                    error.columna);
            }
            System.out.println("------------------------------------------");
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
        
        reader.close();
    }
    
}