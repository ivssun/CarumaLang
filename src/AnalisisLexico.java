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
        
        ErrorLexico(String mensaje, int linea, int columna) {
            this.mensaje = mensaje;
            this.linea = linea;
            this.columna = columna;
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
                System.err.println("✗ Error: No se pudo encontrar el archivo: " + fileName);
            } catch (IOException e) {
                System.err.println("✗ Error al leer el archivo: " + e.getMessage());
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
        System.out.println("----------------------------------------");
        
        boolean continuar = true;
        while (continuar) {
            try {
                Token token = lexer.getNextToken();
                
                if (token.kind == CarumaLangLexerConstants.EOF) {
                    continuar = false;
                } else {
                    tokensValidos.add(token);
                    String tokenName = CarumaLangLexerConstants.tokenImage[token.kind];
                    System.out.printf("%-3d | %-20s | %-30s | Línea: %d, Col: %d%n", 
                        tokensValidos.size(),
                        token.image,
                        tokenName,
                        token.beginLine,
                        token.beginColumn);
                }
            } catch (TokenMgrError e) {
                // Capturar información del error
                String mensaje = e.getMessage();
                int linea = stream.getEndLine();
                int columna = stream.getEndColumn();
                
                errores.add(new ErrorLexico(mensaje, linea, columna));
                
                // Intentar recuperarse: avanzar un carácter
                try {
                    stream.readChar();
                } catch (IOException ioException) {
                    continuar = false;
                }
            }
        }
        
        System.out.println("----------------------------------------");
        
        // Mostrar errores si los hay
        if (!errores.isEmpty()) {
            System.out.println("\nERRORES LÉXICOS ENCONTRADOS:");
            System.out.println("----------------------------------------");
            for (int i = 0; i < errores.size(); i++) {
                ErrorLexico error = errores.get(i);
                System.out.printf("Error %d: %s (Línea: %d, Col: %d)%n", 
                    i + 1, 
                    error.mensaje,
                    error.linea,
                    error.columna);
            }
            System.out.println("----------------------------------------");
        }
        
        // Resumen final
        System.out.println("\nRESUMEN DEL ANÁLISIS:");
        System.out.println("----------------------------------------");
        System.out.println("Tokens válidos: " + tokensValidos.size());
        System.out.println("Errores encontrados: " + errores.size());
        
        if (errores.isEmpty()) {
            System.out.println("\nAnálisis léxico completado SIN ERRORES");
        } else {
            System.out.println("\nAnálisis completado CON ERRORES");
        }
        System.out.println("========================================");
        
        reader.close();
    }
}