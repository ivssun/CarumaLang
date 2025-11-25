import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import AnalizadorLexico.*;
import Arbol.*;

public class AnalizadorSintactico {

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivo CarumaLang", "crm");
        fileChooser.setFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();

            if (!fileName.toLowerCase().endsWith(".crm")) {
                System.err.println("Error: El archivo seleccionado no tiene una extensión .crm");
                return;
            }

            try {
                analizarArchivo(fileName);
            } catch (FileNotFoundException e) {
                System.err.println("✗ Error: No se pudo encontrar el archivo: " + fileName);
            } catch (IOException e) {
                System.err.println("✗ Error al leer el archivo: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("✗ Error durante el análisis: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No se seleccionó ningún archivo.");
        }
    }

    private static void analizarArchivo(String fileName) throws Exception {
        System.out.println("========================================");
        System.out.println("   ANALIZADOR SINTÁCTICO - CARUMALANG");
        System.out.println("========================================");
        System.out.println("Archivo: " + fileName + "\n");

        // FASE 1: ANÁLISIS LÉXICO
        System.out.println("========================================");
        System.out.println("   FASE 1: ANÁLISIS LÉXICO");
        System.out.println("========================================\n");

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
                String mensaje = e.getMessage();
                int linea = stream.getEndLine();
                int columna = stream.getEndColumn();

                errores.add(new ErrorLexico(mensaje, linea, columna));

                try {
                    stream.readChar();
                } catch (IOException ioException) {
                    continuar = false;
                }
            }
        }

        System.out.println("----------------------------------------");

        // Mostrar errores léxicos si los hay
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
            System.out.println("\n❌ No se puede continuar con el análisis sintáctico debido a errores léxicos.");
            reader.close();
            return;
        }

        System.out.println("\n✓ Análisis léxico completado exitosamente.");
        System.out.println("Total de tokens: " + tokensValidos.size());

        // FASE 2: ANÁLISIS SINTÁCTICO
        System.out.println("\n========================================");
        System.out.println("   FASE 2: ANÁLISIS SINTÁCTICO");
        System.out.println("========================================\n");

        try {
            ParserCaruma parser = new ParserCaruma(tokensValidos);
            NodoPrograma arbol = parser.parsearPrograma();

            System.out.println("✓ Análisis sintáctico completado exitosamente.\n");

            // FASE 3: MOSTRAR ÁRBOL DE DERIVACIÓN
            System.out.println("========================================");
            System.out.println("   ÁRBOL DE DERIVACIÓN");
            System.out.println("========================================\n");

            arbol.imprimir();

            System.out.println("\n========================================");
            System.out.println("   ANÁLISIS COMPLETADO");
            System.out.println("========================================");
            System.out.println("✓ Programa sintácticamente correcto");
            System.out.println("✓ Árbol de derivación generado");

        } catch (Exception e) {
            System.out.println("Error de sintaxis: " + e.getMessage());
            System.out.println("\n========================================");
            System.out.println("   ANÁLISIS FALLIDO");
            System.out.println("========================================");
            throw e;
        }

        reader.close();
    }

    // Clase para almacenar información de errores léxicos
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
}