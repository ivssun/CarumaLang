package Arbol;

/**
 * Nodo para sentencia IF (CaeCliente)
 */
 class NodoIf extends NodoArbol {
     NodoIf() {
        super("IF");
    }
}

/**
 * Nodo para sentencia WHILE (papoi)
 */
 class NodoWhile extends NodoArbol {
     NodoWhile() {
        super("WHILE");
    }
}

/**
 * Nodo para sentencia FOR (paraPapoi)
 */
 class NodoFor extends NodoArbol {
     NodoFor() {
        super("FOR");
    }
}

/**
 * Nodo para sentencia PRINT (holahola)
 */
 class NodoPrint extends NodoArbol {
     NodoPrint() {
        super("PRINT");
    }
}

/**
 * Nodo para declaración de variable
 */
 class NodoDeclaracion extends NodoArbol {
     NodoDeclaracion() {
        super("DECLARACION");
    }
}

/**
 * Nodo para asignación
 */
 class NodoAsignacion extends NodoArbol {
     NodoAsignacion() {
        super("ASIGNACION");
    }
}

/**
 * Nodo para expresión
 */
 class NodoExpresion extends NodoArbol {
     NodoExpresion(String operador) {
        super("EXPRESION", operador);
    }

     NodoExpresion() {
        super("EXPRESION");
    }
}

/**
 * Nodo para condición
 */
 class NodoCondicion extends NodoArbol {
     NodoCondicion() {
        super("CONDICION");
    }
}

/**
 * Nodo para bloque de código
 */
 class NodoBloque extends NodoArbol {
     NodoBloque() {
        super("BLOQUE");
    }
}

/**
 * Nodo para terminal (hoja del árbol)
 */
 class NodoTerminal extends NodoArbol {
     NodoTerminal(String tipo, String valor) {
        super(tipo, valor);
    }
}

/**
 * Nodo para tipo de dato
 */
 class NodoTipo extends NodoArbol {
     NodoTipo(String tipo) {
        super("TIPO", tipo);
    }
}

/**
 * Nodo para operador
 */
 class NodoOperador extends NodoArbol {
     NodoOperador(String operador) {
        super("OPERADOR", operador);
    }
}

/**
 * Nodo para programa completo
 */
public class NodoPrograma extends NodoArbol {
     NodoPrograma() {
        super("PROGRAMA");
    }
}

/**
 * Nodo para RETURN (byebye)
 */
 class NodoReturn extends NodoArbol {
     NodoReturn() {
        super("RETURN");
    }
}

/**
 * Nodo para BREAK (stopPlease)
 */
 class NodoBreak extends NodoArbol {
     NodoBreak() {
        super("BREAK");
    }
}