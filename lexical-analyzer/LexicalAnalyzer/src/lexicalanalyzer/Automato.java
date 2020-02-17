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
        while (a != EOF) {//aqui ele lê e manda pro automato correspondente até o fima do arquivo.
            testaCaractere(a);//automato correspondente
            a = proximo();
        }
    }

    /**
     * Método que lista a lista de tokens incorreta 
    */
    public ArrayList<String> getListarErros() {
        return listarErros;
    }

    /**
     *Método que lista a lista de tokens correta 
    */
    public ArrayList<Token> getListarTokens() {
        return listarTokens;
    }


    private char proximo() {
        if (!codigo.isEmpty()) {

            //separa todos os caracteres de uma determinada linha em um array
            char c[] = codigo.get(linha).toCharArray();

            //verifica se a linha está vazia
            if (c.length == aux) {
                linhaVazia = false;
                return ' ';
                //verifica se a linha for maior que zero retorna a primeira posição do array  
            } else if (c.length > aux) {
                linhaVazia = false;
                return c[aux];
                //verifica se o tamanho do arquivo tem uma próxima linha
            } else if (codigo.size() > (linha + 1)) {
                linha++;//se tiver ele incrementa a linha
                c = codigo.get(linha).toCharArray();
                //c recebe como array a próxima linha
                aux = 0;//posição volta a ser 0 (é mudado dentro dos métodos)
                if (c.length == 0) {//se a linha atual for igual a zero a linha está vazia
                    this.linhaVazia = true;
                    return ' ';
                }
                return c[aux];//se não retorna a primeira posição da linha
            } else {
                return EOF;
            }
        } else {
            return EOF;
        }
    }

    /**
     * Método do automato geral
     * ele recebe o primeiro caractere de uma palavra e envia pro autômato correspondente
     * Ex.: a -> automato letra para classificar identificador ou palavra reservada
     * Caso não se encaixe nos autômatos é enviado para palavra inválida
     * Ex. £ -> palavra inválida
    */
    private void testaCaractere(char a) {
        String lexema;
        if (!this.linhaVazia) {

            lexema = "";
            if (token.verificarEspaco(a)) {//desconsidera espaço
                aux++;
            } else if (token.verificarLetra(a)) {//se for uma letra só poderá ser identificador ou palavra reservada
                letra(lexema, a);
            } else if (Character.isDigit(a)) {//se for digito será enviado para o método de número
                numero(lexema, a);
            } else if (token.verificarOperador(a)) {//se for um operador será enviado para o método de operadores
                operador(lexema, a);
            } else if (token.verificarDelimitador(a)) {//método de delimitador
                delimitador(lexema, a);
            } else if (a == '/') {//método de comentário, por lá verifica também se ele for um operador aritmético
                comentario(lexema, a);
            } else if (a == '"') {//método de cadeia de caractere
                cadeiaDeCaractere(lexema, a);
            } else {//se não entrar em nenhum dessas opções acima é um simbolo incorreto, pois não se encontra na tabela
                this.palavraInvalida(lexema, a);
            }

        } else {
            //linha vazia avança pra próxima
            linhaVazia = false;
            linha++;
        }
    }

    /**
     * Método que identifica a palavra como identificador ou palavra reservada
     * Para chegar nesse autômata a palava deve ser iniciada por uma letra
     * O autômato forma o lexema e o classifica
     * Caso seja uma palavra contida na lista de palavras reservadas ele o classifica dessa forma
     * Caso seja uma palavra formada por letras, digito e o caractere '_' o classifica como identificador
     * Se não se aplica a nenhum dos dois casos é classificado como identificador errado
    */
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

    /**
     * Método que identifica número
     * Ele só inicia se o seu primeiro caractere for um digito
     * Ele monta a palavra sempre que o caractere que vier for um digito ex.: 2333
     * Quando o numero for real é necessário que verifique se já houve o caracter '.'
     * Caso já tenha ele notifica erro ex.: 2.2.2
     * Se não tiver modifica a variável de ponto e continua recebendo digitos ex.: 2.2
     * Se o ponto vier e termina a palavra notifica erro ex.: 2.
    */
    private void numero(String lexema, char a) {
        int linhaInicial = linha;
        int auxiliar = aux;
        boolean ponto = false;
        boolean erro = false;

        lexema = lexema + a;
        this.aux++;
        a = this.proximo();
        while (!(a == EOF || Character.isSpaceChar(a) || token.verificarOperador(a) || token.verificarDelimitador(a) || a == '/' || a == '"')) {

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

    /**
     * Método que identifica o tipo de operador recebido 
     * Caso seja operador Aritimetico é enviado para o método correspondente
     * Case seja operador Relacional ou lógigo é enviado para o método correspondente
     * Essa junção de métodos de operador foi feita para simplificar a condição de parada dos demais autômatos     * 
    */
    private void operador(String lexema, char a) {
        if (a == '+' || a == '-' || a == '*') {
            operadorAritimetico(lexema, a);
            return;
        } else {
            operadorRelacionalLogico(lexema, a);
            return;
        }
    }

    /**
     * Métodos que identificam operadores aritméticos
     * Eles podem ser  '+', '-' , '*', '/'
     * Esse método só não trata o sinal de divisão, pois ele é usado em outro autômato para identificar comentário
     * Caso receba o '+' ele verifica se o proximo é outro '+'
     * Se for ele é incremento se não ele é um sinal de adição normal
     * Caso receba um '*' já o classifica como operador Aritmetico
     * Caso receba '-' ele faz a mesma verificação do '+' e além dela verifica se o próximo caractere é espaço ou digito
     * Se for espaço ele consome até o próximo válido, 
     * Se o proximo válido dor um digito ele envia pro autômato de número
     * Se não é apenas um caractere de subtração.
    */
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

    /**
     * Método que identifica operadores relacionais ou lógicos
     * Eles podem ser '>', '<', '=', '<=', '>=', '==', '!', '!=', '&&', '||'
     * Esse método tem o mesmo funcionamento do métode de operação aritimética com o caracter '+'
    */
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
    /**
     * Método que identifica os caracteres como delimitadores
     * Dentre a Estrutura Léxica dada o único que não é identificado no método é o ponto '.' 
     * Isso se dá pelo critério de parada no método de número
     * O ponto é tratado no método de palavra inválida e é classificado como delimitador
    */
    private void delimitador(String lexema, char a) {
        int linhaInicial = this.linha;
        int auxiliar = this.aux;

        lexema = lexema + a;
        this.aux++;
        Token tokenAuxiliar = new Token(linhaInicial + 1, auxiliar + 1, "delimitador", lexema);
        listarTokens.add(tokenAuxiliar);
    }

    /**
     * Método que identifica comentário
     * Ao receber um '/' verifica qual é o próximo caractere
     * Ee for outro '/' ele consome todos os caracteres da linha atual e o classifica como comentário
     * Se for '*' ele consome até encontrar outro '*' e em seguida outra '/' e o classifica como comentário
     * Caso esse último não esteja completo é dado como erro
     * Qualquer outro caracter seguido do primeiro '/' que não se encaixe em nenhuma das duas condições acima 
     * O caractere é considerado opreador aritimetico
     * 
    */
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

    /**
     * Método que identifica a cadeia de caractere
     * Inicia quando recebe '"' e pode conter letra, digito, '\"' e simbolos válidos
     * caso não encerre ao final da linha com '"' ou contenha simbolo inválido ele notifica erro.
    */
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

    /**
     * Qualquer palavra que não se encaixe em nenhum dos autômatos é dado como palavra inválida
     * Caso chegue um ponto '.' em qualquer local do código exceto no número ele é classificado como delimitador
     * Esse caso é tratado nesse método
    */
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

    /**
     * Método que adiciona o erro em uma lista 
    */
    private void addErro(String tipo, String erro, int linha) {
        listarErros.add((linha + 1) + "  " + erro + "  " + tipo + "  ");
    }
}
