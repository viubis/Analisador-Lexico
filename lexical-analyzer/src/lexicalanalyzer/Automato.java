/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexicalanalyzer;

import java.util.ArrayList;

/**
 *
 * @author Víctor Souza e Pedro Brandão
 */
public class Automato {

    private final ArrayList<Token> listarTokens;
    private final ArrayList<String> listarErros;
    private ArrayList<String> codigo;
    private static final char EOF = '\0';
    private int linha, aux;
    private boolean linhaVazia;
    private final EstruturaLexica token;

    public Automato() {
        this.listarTokens = new ArrayList<>();
        this.listarErros = new ArrayList<>();
        this.codigo = new ArrayList<>();
        this.linha = 0;
        this.aux = 0;
        this.linhaVazia = false;
        this.token = new EstruturaLexica();
    }
    void analisadorLexico(ArrayList<String> codigoFonte) {
        this.codigo = codigoFonte;
        char a = proximo();
        while (a != EOF) {
            testaCaractere(a);
            a = proximo();
        }
    }

    public ArrayList<String> getListarErros() {
        return listarErros;
    }

    
    public ArrayList<Token> getListarTokens() {
        return listarTokens;
    }


    private char proximo() {
        if (!codigo.isEmpty()) {
            char c[] = codigo.get(linha).toCharArray();
            if (c.length == aux) {
                linhaVazia = false;
                return ' ';
            } else if (c.length > aux) {
                linhaVazia = false;
                return c[aux];
            } else if (codigo.size() > (linha + 1)) {
                linha++;
                c = codigo.get(linha).toCharArray();
                aux = 0;
                if (c.length == 0) {
                    this.linhaVazia = true;
                    return ' ';
                }
                return c[aux];
            } else {
                return EOF;
            }
        } else {
            return EOF;
        }
    }

    private void testaCaractere(char a) {
        String lexema;
        if (!this.linhaVazia) {

            lexema = "";
            if (token.verificarEspaco(a)) {
                aux++;
            } else if (token.verificarLetra(a)) {
                letra(lexema, a);
            } else if (Character.isDigit(a)) {
                numero(lexema, a);
            } else if (token.verificarOperador(a)) {
                operador(lexema, a);
            } else if (token.verificarDelimitador(a)) {
                delimitador(lexema, a);
            } else if (a == '"') {
                cadeiaDeCaractere(lexema, a);
            } else if (a == '/') {
                comentario(lexema, a);
            } else {
                this.palavraInvalida(lexema, a);
            }

        } else {
            linhaVazia = false;
            linha++;
        }
    }

    public void letra(String lexema, char a) {

        int linhaInicial = linha;
        int aux1 = aux;
        boolean erro = false;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();

        while (!(a == EOF || Character.isSpaceChar(a) || token.verificarDelimitador(a) || token.verificarOperador(a) || a == '/' || a == '"')) {
            if (!(a == '_' || token.verificarLetra(a) || Character.isDigit(a))) {
                erro = true;
            }
            lexema = lexema + a;
            aux++;
            a = this.proximo();
        }
        if (!erro) {
            Token tokenaux;
            if (token.verificarPalavrasReservada(lexema)) {
                tokenaux = new Token(linhaInicial + 1, aux1 + 1, "palavraReservada", lexema);
            } else {
                tokenaux = new Token(linhaInicial + 1, aux1 + 1, "identificador", lexema);
            }
            listarTokens.add(tokenaux);
        } else {
            this.addErro("identificadorErrado", lexema, linhaInicial);
        }
    }

    private void numero(String lexema, char a) {
        int linhaInicial = linha;
        int auxiliar = aux;
        boolean ponto = false;
        boolean erro = false;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();
        while (!(a == EOF || Character.isSpaceChar(a) || token.verificarOperador(a) || 
                token.verificarDelimitador(a) || a == '/' || a == '"')) {

            if (!(Character.isDigit(a)) && a != '.') {
                erro = true;
                lexema = lexema + a;
                aux++;
                a = this.proximo();
            } else if (Character.isDigit(a)) {
                lexema = lexema + a;
                aux++;
                a = this.proximo();
            } else if (a == '.' && ponto == false) {
                lexema = lexema + a;
                aux++;
                ponto = true;
                a = this.proximo();
                if (!(Character.isDigit(a))) {
                    erro = true;
                }
            } else {
                erro = true;
                lexema = lexema + a;
                aux++;
                a = this.proximo();
            }
        }
        if (!erro) {
            Token tokenAuxiliar;
            tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "numero", lexema);
            listarTokens.add(tokenAuxiliar);
        } else {
            addErro("numeroErrado", lexema, linhaInicial);
        }
    }

    private void operador(String lexema, char a) {
        if (a == '+' || a == '*' || a == '-' ) {
            operadorAritimetico(lexema, a);
            return;
        } else {
            operadorRelacionalLogico(lexema, a);
            return;
        }
    }

    private void operadorAritimetico(String lexema, char a) {
        int linhaInicial = this.linha;
        int auxiliar = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;

        if (a == '+') {
            a = this.proximo();
            if (a == '+') {
                lexema = lexema + a;
                this.aux++;
            }
        } else if (a == '-') {
            a = this.proximo();
            if (Character.isSpaceChar(a)) {
                do {
                    this.aux++;
                    a = this.proximo();
                } while (token.verificarEspaco(a));
                if (Character.isDigit(a)) {
                    this.numero(lexema, a);
                    return;
                }
            } else if (a == '-') {
                lexema = lexema + a;
                this.aux++;
            } else if (Character.isDigit(a)) {
                this.numero(lexema, a);
                return;
            }

        }
        tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opAritmetico", lexema);
        listarTokens.add(tokenAuxiliar);
    }

    private void operadorRelacionalLogico(String lexema, char a) {
        int linhaInicial = this.linha;
        int auxiliar = this.aux;
        Token tokenAuxiliar;

        lexema = lexema + a;
        this.aux++;

        switch (a) {
            case '<':
            case '>':
            case '=':
                a = this.proximo();
                if (a == '=') {
                    lexema = lexema + a;
                    this.aux++;
                }   tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opRelacional", lexema);
                listarTokens.add(tokenAuxiliar);
                break;
            case '!':
                a = this.proximo();
                if (a == '=') {
                    lexema = lexema + a;
                    this.aux++;
                    tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opRelacional", lexema);
                    listarTokens.add(tokenAuxiliar);
                }else{
                    tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opLogico", lexema);
                    listarTokens.add(tokenAuxiliar);
                }   break;
            case '&':
                a = this.proximo();
                if (a == '&') {
                    lexema = lexema + a;
                    this.aux++;
                    tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opLogico", lexema);
                    listarTokens.add(tokenAuxiliar);
                } else {
                    this.addErro("opLogicoErrado", lexema, linhaInicial);
                    return;
                }   break;
            case '|':
                a = this.proximo();
                if (a == '|') {
                    lexema = lexema + a;
                    this.aux++;
                    tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opLogico", lexema);
                    listarTokens.add(tokenAuxiliar);
                } else {
                    this.addErro("opLogicoErrado", lexema, linhaInicial);
                    return;
                }   break;
            default:
                break;
        }
    }
 
    private void delimitador(String lexema, char a) {
        int linhaInicial = this.linha;
        int auxiliar = this.aux;

        lexema = lexema + a;
        this.aux++;
        Token tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "delimitador", lexema);
        listarTokens.add(tokenAuxiliar);
    }

    private void comentario(String lexema, char a) {
        int linhaInicial = this.linha;
        int auxiliar = this.aux;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();
        
        switch (a) {
            case '/':
                {
                    lexema = lexema + a;
                    this.aux++;
                    a = this.proximo();
                    while (linha == linhaInicial && a != EOF) {
                        lexema = lexema + a;
                        this.aux++;
                        a = this.proximo();
                    }       Token tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "comentario", lexema);
                    this.listarTokens.add(tokenAuxiliar);
                    break;
                }
            case '*':
                lexema = lexema + a;
                this.aux++;
                a = this.proximo();
                while (a != '*' && a != EOF) {
                    lexema = lexema + a;
                    this.aux++;
                    a = this.proximo();
                }   if (a == '*') {
                    lexema = lexema + a;
                    this.aux++;
                    a = this.proximo();
                    if (a == '/') {
                        lexema = lexema + a;
                        this.aux++;
                        a = this.proximo();
                        Token tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "comentario", lexema);
                        this.listarTokens.add(tokenAuxiliar);
                    } else {
                        this.addErro("comentarioErrado", lexema, linhaInicial);
                    }
                } else if (a == EOF){
                    this.addErro("comentarioErrado", lexema, linhaInicial);
                }   break;
            default:
                {
                    Token tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "opArimetico", lexema);
                    this.listarTokens.add(tokenAuxiliar);
                    break;
                }
        }
    }

    private void cadeiaDeCaractere(String lexema, char a) {

        int linhaInicial = this.linha;
        boolean simboloInvalido = false;
        int auxiliar = this.aux;
        boolean erro = false;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();

        while (a != '"' && linha == linhaInicial && a != EOF) {
            if (a == ((char) 92) || Character.isLetterOrDigit(a) || token.verificarSimbolo(a) || Character.isDigit(a)) {
                this.aux++;
                lexema = lexema + a;
                a = this.proximo();               
            } else if (token.verificarSimboloInvalido(a)) {
                this.aux++;
                lexema = lexema + a;
                a = this.proximo();
                erro = true;
                simboloInvalido = true;
            }else { 
                this.aux++;
                lexema = lexema + a;
                a = this.proximo();
                erro = true;
            }
        }

        if (a == '"' && linhaInicial == this.linha) {
            lexema = lexema + a;
            this.aux++;
        } else
        {
            erro = true;
        }

        if (!erro && linhaInicial == this.linha) {
            Token tokenAuxiliar;
            tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "cadeiaDeCaractere", lexema);
            this.listarTokens.add(tokenAuxiliar);
        } else if (simboloInvalido == true) {
            this.addErro("cadeiaDeCaractereErrada", lexema, linhaInicial);
        } else {
            this.addErro("cadeiaDeCaractereErrada", lexema, linhaInicial + 1);
        }
    }

    private void palavraInvalida(String lexema, char a) {

        int linhaInicial = this.linha;
        int auxiliar = this.aux;

        if (a == '.') {
            lexema = lexema + a;
            this.aux++;
            Token tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "delimitador", lexema);
            listarTokens.add(tokenAuxiliar);
        } else {
            while (!(a == EOF || Character.isSpaceChar(a) || token.verificarOperador(a) || token.verificarDelimitador(a) || a == '/' || a == '"')) {
                lexema = lexema + a;
                this.aux++;
                a = proximo();
            }
            this.addErro("palavraInvalida", lexema, linhaInicial);
        }
    }

    private void addErro(String tipo, String erro, int linha) {
        listarErros.add((linha + 1) + "  " + erro + "  " + tipo + "  ");
    }
}
