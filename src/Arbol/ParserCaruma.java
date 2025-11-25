package Arbol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import AnalizadorLexico.*;

/**
 * Parser descendente recursivo que construye árboles de derivación
 */
public class ParserCaruma {
    private List<Token> tokens;
    private int posicionActual;
    private Token tokenActual;

    public ParserCaruma(List<Token> tokens) {
        this.tokens = tokens;
        this.posicionActual = 0;
        if (!tokens.isEmpty()) {
            this.tokenActual = tokens.get(0);
        }
    }

    /**
     * Avanza al siguiente token
     */
    private void avanzar() {
        posicionActual++;
        if (posicionActual < tokens.size()) {
            tokenActual = tokens.get(posicionActual);
        } else {
            tokenActual = null;
        }
    }

    /**
     * Verifica si el token actual coincide con el tipo esperado
     */
    private boolean coincide(int tipoEsperado) {
        return tokenActual != null && tokenActual.kind == tipoEsperado;
    }

    /**
     * Consume un token esperado o lanza error
     */
    private Token consumir(int tipoEsperado, String mensaje) throws Exception {
        if (coincide(tipoEsperado)) {
            Token t = tokenActual;
            avanzar();
            return t;
        }
        throw new Exception(mensaje + " en línea " +
                (tokenActual != null ? tokenActual.beginLine : "?"));
    }

    /**
     * Parsea el programa completo
     * Programa → Caruma Sentencias byebye
     */
    public NodoPrograma parsearPrograma() throws Exception {
        NodoPrograma programa = new NodoPrograma();

        // Caruma
        Token caruma = consumir(CarumaLangLexerConstants.CARUMA,
                "Se esperaba 'Caruma' al inicio del programa");
        programa.agregarHijo(new NodoTerminal("CARUMA", caruma.image));

        // Sentencias
        while (tokenActual != null &&
                tokenActual.kind != CarumaLangLexerConstants.BYEBYE) {
            NodoArbol sentencia = parsearSentencia();
            if (sentencia != null) {
                programa.agregarHijo(sentencia);
            }
        }

        // byebye
        Token byebye = consumir(CarumaLangLexerConstants.BYEBYE,
                "Se esperaba 'byebye' al final del programa");
        programa.agregarHijo(new NodoTerminal("BYEBYE", byebye.image));

        return programa;
    }

    /**
     * Parsea una sentencia
     */
    private NodoArbol parsearSentencia() throws Exception {
        if (tokenActual == null) return null;

        switch (tokenActual.kind) {
            case CarumaLangLexerConstants.CAECLIENTE:
                return parsearIf();

            case CarumaLangLexerConstants.PAPOI:
                return parsearWhile();

            case CarumaLangLexerConstants.PARAPAPOI:
                return parsearFor();

            case CarumaLangLexerConstants.HOLAHOLA:
                return parsearPrint();

            case CarumaLangLexerConstants.INTCHELADA:
            case CarumaLangLexerConstants.GRANITO:
            case CarumaLangLexerConstants.CADENA:
            case CarumaLangLexerConstants.CARACTER:
                return parsearDeclaracion();

            case CarumaLangLexerConstants.MIXCHELADA:
                return parsearAsignacion();

            case CarumaLangLexerConstants.BYEBYE:
                return null; // Fin del programa

            case CarumaLangLexerConstants.STOPPLEASE:
                return parsearBreak();

            default:
                throw new Exception("Sentencia no reconocida en línea " +
                        tokenActual.beginLine);
        }
    }

    /**
     * Parsea IF (CaeCliente)
     * I → CaeCliente ( C ) { B }
     * I → CaeCliente ( C ) { B } SiNoCae { B }
     */
    private NodoIf parsearIf() throws Exception {
        NodoIf nodoIf = new NodoIf();

        // CaeCliente
        Token caeCliente = consumir(CarumaLangLexerConstants.CAECLIENTE,
                "Se esperaba 'CaeCliente'");
        nodoIf.setLinea(caeCliente.beginLine);
        nodoIf.agregarHijo(new NodoTerminal("CaeCliente", caeCliente.image));

        // (
        consumir(CarumaLangLexerConstants.ABRIENDO, "Se esperaba '('");

        // Condición
        NodoCondicion condicion = parsearCondicion();
        nodoIf.agregarHijo(condicion);

        // )
        consumir(CarumaLangLexerConstants.CERRANDO, "Se esperaba ')'");

        // {
        consumir(CarumaLangLexerConstants.OPEN, "Se esperaba '{'");

        // Bloque
        NodoBloque bloqueThen = parsearBloque();
        nodoIf.agregarHijo(bloqueThen);

        // }
        consumir(CarumaLangLexerConstants.CLOSE, "Se esperaba '}'");

        // SiNoCae (opcional)
        if (coincide(CarumaLangLexerConstants.SINOCAE)) {
            Token siNoCae = consumir(CarumaLangLexerConstants.SINOCAE, "");
            nodoIf.agregarHijo(new NodoTerminal("SiNoCae", siNoCae.image));

            // {
            consumir(CarumaLangLexerConstants.OPEN, "Se esperaba '{'");

            // Bloque else
            NodoBloque bloqueElse = parsearBloque();
            nodoIf.agregarHijo(bloqueElse);

            // }
            consumir(CarumaLangLexerConstants.CLOSE, "Se esperaba '}'");
        }

        return nodoIf;
    }

    /**
     * Parsea WHILE (papoi)
     * W → papoi ( C ) { B }
     */
    private NodoWhile parsearWhile() throws Exception {
        NodoWhile nodoWhile = new NodoWhile();

        // papoi
        Token papoi = consumir(CarumaLangLexerConstants.PAPOI,
                "Se esperaba 'papoi'");
        nodoWhile.setLinea(papoi.beginLine);
        nodoWhile.agregarHijo(new NodoTerminal("papoi", papoi.image));

        // (
        consumir(CarumaLangLexerConstants.ABRIENDO, "Se esperaba '('");

        // Condición
        NodoCondicion condicion = parsearCondicion();
        nodoWhile.agregarHijo(condicion);

        // )
        consumir(CarumaLangLexerConstants.CERRANDO, "Se esperaba ')'");

        // {
        consumir(CarumaLangLexerConstants.OPEN, "Se esperaba '{'");

        // Bloque
        NodoBloque bloque = parsearBloque();
        nodoWhile.agregarHijo(bloque);

        // }
        consumir(CarumaLangLexerConstants.CLOSE, "Se esperaba '}'");

        return nodoWhile;
    }

    /**
     * Parsea FOR (paraPapoi)
     * F → paraPapoi ( C ) { B }
     * Nota: Simplificado, no usa Init:Cond:Inc como en la gramática formal
     */
    private NodoFor parsearFor() throws Exception {
        NodoFor nodoFor = new NodoFor();

        // paraPapoi
        Token paraPapoi = consumir(CarumaLangLexerConstants.PARAPAPOI,
                "Se esperaba 'paraPapoi'");
        nodoFor.setLinea(paraPapoi.beginLine);
        nodoFor.agregarHijo(new NodoTerminal("paraPapoi", paraPapoi.image));

        // (
        consumir(CarumaLangLexerConstants.ABRIENDO, "Se esperaba '('");

        // Condición
        NodoCondicion condicion = parsearCondicion();
        nodoFor.agregarHijo(condicion);

        // )
        consumir(CarumaLangLexerConstants.CERRANDO, "Se esperaba ')'");

        // {
        consumir(CarumaLangLexerConstants.OPEN, "Se esperaba '{'");

        // Bloque
        NodoBloque bloque = parsearBloque();
        nodoFor.agregarHijo(bloque);

        // }
        consumir(CarumaLangLexerConstants.CLOSE, "Se esperaba '}'");

        return nodoFor;
    }

    /**
     * Parsea PRINT (holahola)
     * H → holahola ( A )
     */
    private NodoPrint parsearPrint() throws Exception {
        NodoPrint nodoPrint = new NodoPrint();

        // holahola
        Token holahola = consumir(CarumaLangLexerConstants.HOLAHOLA,
                "Se esperaba 'holahola'");
        nodoPrint.setLinea(holahola.beginLine);
        nodoPrint.agregarHijo(new NodoTerminal("holahola", holahola.image));

        // (
        consumir(CarumaLangLexerConstants.ABRIENDO, "Se esperaba '('");

        // Argumento (expresión, string, id, etc.)
        NodoArbol argumento = parsearArgumento();
        nodoPrint.agregarHijo(argumento);

        // )
        consumir(CarumaLangLexerConstants.CERRANDO, "Se esperaba ')'");

        return nodoPrint;
    }

    /**
     * Parsea declaración de variable
     * D → tipo id
     * D → tipo id ESTOES E
     */
    private NodoDeclaracion parsearDeclaracion() throws Exception {
        NodoDeclaracion nodoDecl = new NodoDeclaracion();

        // Tipo
        Token tipo = tokenActual;
        nodoDecl.setLinea(tipo.beginLine);

        String nombreTipo = "";
        switch (tipo.kind) {
            case CarumaLangLexerConstants.INTCHELADA:
                nombreTipo = "intCHELADA";
                break;
            case CarumaLangLexerConstants.GRANITO:
                nombreTipo = "granito";
                break;
            case CarumaLangLexerConstants.CADENA:
                nombreTipo = "cadena";
                break;
            case CarumaLangLexerConstants.CARACTER:
                nombreTipo = "caracter";
                break;
        }
        avanzar();
        nodoDecl.agregarHijo(new NodoTipo(nombreTipo));

        // Identificador
        Token id = consumir(CarumaLangLexerConstants.MIXCHELADA,
                "Se esperaba un identificador");
        nodoDecl.agregarHijo(new NodoTerminal("ID", id.image));

        // Asignación opcional
        if (coincide(CarumaLangLexerConstants.ESTOES)) {
            Token estoes = consumir(CarumaLangLexerConstants.ESTOES, "");
            nodoDecl.agregarHijo(new NodoOperador("="));

            // Expresión
            NodoArbol expr = parsearExpresion();
            nodoDecl.agregarHijo(expr);
        }

        return nodoDecl;
    }

    /**
     * Parsea asignación
     * A → id ESTOES E
     */
    private NodoAsignacion parsearAsignacion() throws Exception {
        NodoAsignacion nodoAsig = new NodoAsignacion();

        // Identificador
        Token id = consumir(CarumaLangLexerConstants.MIXCHELADA,
                "Se esperaba un identificador");
        nodoAsig.setLinea(id.beginLine);
        nodoAsig.agregarHijo(new NodoTerminal("ID", id.image));

        // =
        consumir(CarumaLangLexerConstants.ESTOES, "Se esperaba '='");
        nodoAsig.agregarHijo(new NodoOperador("="));

        // Expresión
        NodoArbol expr = parsearExpresion();
        nodoAsig.agregarHijo(expr);

        return nodoAsig;
    }

    /**
     * Parsea BREAK (stopPlease)
     */
    private NodoBreak parsearBreak() throws Exception {
        Token stopPlease = consumir(CarumaLangLexerConstants.STOPPLEASE,
                "Se esperaba 'stopPlease'");
        NodoBreak nodoBreak = new NodoBreak();
        nodoBreak.setLinea(stopPlease.beginLine);
        nodoBreak.agregarHijo(new NodoTerminal("stopPlease", stopPlease.image));
        return nodoBreak;
    }

    /**
     * Parsea una condición
     * C → E op E
     * C → DIOS | DIOSNO
     */
    private NodoCondicion parsearCondicion() throws Exception {
        NodoCondicion condicion = new NodoCondicion();

        // Verificar si es un booleano literal
        if (coincide(CarumaLangLexerConstants.DIOS)) {
            Token dios = consumir(CarumaLangLexerConstants.DIOS, "");
            condicion.agregarHijo(new NodoTerminal("DIOS", dios.image));
            return condicion;
        }

        if (coincide(CarumaLangLexerConstants.DIOSNO)) {
            Token diosno = consumir(CarumaLangLexerConstants.DIOSNO, "");
            condicion.agregarHijo(new NodoTerminal("DIOSNO", diosno.image));
            return condicion;
        }

        // E op E
        NodoArbol expr1 = parsearExpresionSimple();
        condicion.agregarHijo(expr1);

        // Operador relacional
        Token op = tokenActual;
        String operador = "";
        switch (op.kind) {
            case CarumaLangLexerConstants.MAYORQUE:
                operador = ">";
                break;
            case CarumaLangLexerConstants.MENORQUE:
                operador = "<";
                break;
            case CarumaLangLexerConstants.IGUALITO:
                operador = "==";
                break;
            case CarumaLangLexerConstants.MAYORIGUALITOQUE:
                operador = ">=";
                break;
            case CarumaLangLexerConstants.MENORIGUALITOQUE:
                operador = "<=";
                break;
            default:
                throw new Exception("Operador relacional esperado en línea " +
                        op.beginLine);
        }
        avanzar();
        condicion.agregarHijo(new NodoOperador(operador));

        // Segunda expresión
        NodoArbol expr2 = parsearExpresionSimple();
        condicion.agregarHijo(expr2);

        return condicion;
    }

    /**
     * Parsea un bloque de sentencias
     * B → S | S B
     */
    private NodoBloque parsearBloque() throws Exception {
        NodoBloque bloque = new NodoBloque();

        while (tokenActual != null &&
                tokenActual.kind != CarumaLangLexerConstants.CLOSE) {
            NodoArbol sentencia = parsearSentencia();
            if (sentencia != null) {
                bloque.agregarHijo(sentencia);
            }
        }

        return bloque;
    }

    /**
     * Parsea un argumento de función (para holahola, byebye, etc.)
     * A → string | id | num | E
     */
    private NodoArbol parsearArgumento() throws Exception {
        if (coincide(CarumaLangLexerConstants.TEXTOLITERAL)) {
            Token str = consumir(CarumaLangLexerConstants.TEXTOLITERAL, "");
            return new NodoTerminal("STRING", str.image);
        }

        if (coincide(CarumaLangLexerConstants.LETRALITERAL)) {
            Token chr = consumir(CarumaLangLexerConstants.LETRALITERAL, "");
            return new NodoTerminal("CHAR", chr.image);
        }

        if (coincide(CarumaLangLexerConstants.MIXCHELADA)) {
            Token id = consumir(CarumaLangLexerConstants.MIXCHELADA, "");
            return new NodoTerminal("ID", id.image);
        }

        if (coincide(CarumaLangLexerConstants.NUMERITO)) {
            Token num = consumir(CarumaLangLexerConstants.NUMERITO, "");
            return new NodoTerminal("NUM", num.image);
        }

        // Si no es ninguno de los anteriores, intentar parsear como expresión
        return parsearExpresion();
    }

    /**
     * Parsea una expresión aritmética
     * E → E + T | E - T | T
     */
    private NodoArbol parsearExpresion() throws Exception {
        NodoArbol nodo = parsearTermino();

        while (coincide(CarumaLangLexerConstants.PONER) ||
                coincide(CarumaLangLexerConstants.QUITAR)) {

            String operador = tokenActual.kind == CarumaLangLexerConstants.PONER
                    ? "+" : "-";
            avanzar();

            NodoExpresion expr = new NodoExpresion(operador);
            expr.agregarHijo(nodo);
            expr.agregarHijo(parsearTermino());
            nodo = expr;
        }

        return nodo;
    }

    /**
     * Parsea un término
     * T → T * F | T / F | F
     */
    private NodoArbol parsearTermino() throws Exception {
        NodoArbol nodo = parsearFactor();

        while (coincide(CarumaLangLexerConstants.SALEMAS) ||
                coincide(CarumaLangLexerConstants.SALEMENOS)) {

            String operador = tokenActual.kind == CarumaLangLexerConstants.SALEMAS
                    ? "*" : "/";
            avanzar();

            NodoExpresion expr = new NodoExpresion(operador);
            expr.agregarHijo(nodo);
            expr.agregarHijo(parsearFactor());
            nodo = expr;
        }

        return nodo;
    }

    /**
     * Parsea un factor
     * F → id | num | ( E )
     */
    private NodoArbol parsearFactor() throws Exception {
        if (coincide(CarumaLangLexerConstants.MIXCHELADA)) {
            Token id = consumir(CarumaLangLexerConstants.MIXCHELADA, "");
            return new NodoTerminal("ID", id.image);
        }

        if (coincide(CarumaLangLexerConstants.NUMERITO)) {
            Token num = consumir(CarumaLangLexerConstants.NUMERITO, "");
            return new NodoTerminal("NUM", num.image);
        }

        if (coincide(CarumaLangLexerConstants.TEXTOLITERAL)) {
            Token str = consumir(CarumaLangLexerConstants.TEXTOLITERAL, "");
            return new NodoTerminal("STRING", str.image);
        }

        if (coincide(CarumaLangLexerConstants.DIOS)) {
            Token dios = consumir(CarumaLangLexerConstants.DIOS, "");
            return new NodoTerminal("DIOS", dios.image);
        }

        if (coincide(CarumaLangLexerConstants.DIOSNO)) {
            Token diosno = consumir(CarumaLangLexerConstants.DIOSNO, "");
            return new NodoTerminal("DIOSNO", diosno.image);
        }

        if (coincide(CarumaLangLexerConstants.ABRIENDO)) {
            consumir(CarumaLangLexerConstants.ABRIENDO, "");
            NodoArbol expr = parsearExpresion();
            consumir(CarumaLangLexerConstants.CERRANDO, "Se esperaba ')'");
            return expr;
        }

        throw new Exception("Factor esperado en línea " +
                (tokenActual != null ? tokenActual.beginLine : "?"));
    }

    /**
     * Parsea una expresión simple (para condiciones)
     */
    private NodoArbol parsearExpresionSimple() throws Exception {
        if (coincide(CarumaLangLexerConstants.MIXCHELADA)) {
            Token id = consumir(CarumaLangLexerConstants.MIXCHELADA, "");
            return new NodoTerminal("ID", id.image);
        }

        if (coincide(CarumaLangLexerConstants.NUMERITO)) {
            Token num = consumir(CarumaLangLexerConstants.NUMERITO, "");
            return new NodoTerminal("NUM", num.image);
        }

        throw new Exception("Expresión esperada en línea " +
                (tokenActual != null ? tokenActual.beginLine : "?"));
    }
}