package Arbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase base para representar nodos en el árbol de derivación
 */
public abstract class NodoArbol {
    protected String tipo;
    protected String valor;
    protected List<NodoArbol> hijos;
    protected int linea;
    protected int columna;

    public NodoArbol(String tipo) {
        this.tipo = tipo;
        this.hijos = new ArrayList<>();
    }

    public NodoArbol(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(NodoArbol hijo) {
        if (hijo != null) {
            hijos.add(hijo);
        }
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public List<NodoArbol> getHijos() {
        return hijos;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    /**
     * Imprime el árbol en formato textual con indentación
     */
    public void imprimir() {
        imprimir("", true);
    }

    private void imprimir(String prefijo, boolean esUltimo) {
        System.out.print(prefijo);
        System.out.print(esUltimo ? "└── " : "├── ");

        if (valor != null) {
            System.out.println(tipo + ": " + valor);
        } else {
            System.out.println(tipo);
        }

        String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");

        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimoHijo = (i == hijos.size() - 1);
            hijos.get(i).imprimir(nuevoPrefijo, ultimoHijo);
        }
    }

    @Override
    public String toString() {
        if (valor != null) {
            return tipo + "(" + valor + ")";
        }
        return tipo;
    }
}