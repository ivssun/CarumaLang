import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import AnalizadorSintactico.*;

/**
 * Clase principal para el Análisis Sintáctico de CarumaLang
 * Implementa un parser LL(1) descendente recursivo
 * 
 * @author Renata Carolina Castro Olmos
 * @author Isaías de Jesús Áviles Rodríguez
 * @author Carlos Alberto Ureña Andrade
 * @author Olimpia de los Angeles Moctezuma Juan
 */
public class AnalisisSintactico {
    
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
        System.out.println("========================================");
        System.out.println("Archivo: " + fileName + "\n");
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            CarumaLangParser parser = new CarumaLangParser(reader);
            
            System.out.println("Iniciando analisis sintactico...\n");
            
            // Llamar a la regla inicial del parser
            parser.Programa();
            
            System.out.println("========================================");
            System.out.println("         ANALISIS EXITOSO");
            System.out.println("========================================");
            System.out.println("\nEl programa cumple con la sintaxis de CarumaLang");
            System.out.println("No se encontraron errores sintacticos");
            System.out.println("\n========================================");
            
        } catch (ParseException e) {
            System.err.println("========================================");
            System.err.println("         ERROR SINTACTICO");
            System.err.println("========================================\n");
            System.err.println("Se encontro un error en la estructura del programa:\n");
            System.err.println(e.getMessage());
            System.err.println("\n========================================");
            
        } catch (TokenMgrError e) {
            System.err.println("========================================");
            System.err.println("         ERROR LEXICO");
            System.err.println("========================================\n");
            System.err.println("Se encontro un error en los tokens:\n");
            System.err.println(e.getMessage());
            System.err.println("\n========================================");
        }
    }
}