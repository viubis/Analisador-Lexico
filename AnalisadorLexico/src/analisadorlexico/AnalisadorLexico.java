/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analisadorlexico;

import java.util.Scanner;

/**
 *
 * @author victo
 */
public class AnalisadorLexico {
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Digite o texto a ser analisado:");
        Scanner ler = new Scanner(System.in);
        String texto;// TODO code application logic here
        texto = ler.next();
        if(null != texto)switch (texto) {
            case "var":
            case "const":
            case "struct":
            case "typedef":
            case "extends":
            case "procedure":
            case "function":
            case "start":
            case "return":
            case "if":
            case "else":
            case "then":
            case "while":
            case "read":
            case "print":
                System.out.println("Palavra reservada");
                break;
            case "0":
            case "1":
            case "2":
            case "3":
                System.out.println("Digitos");
                break;
            case "a":
            case "b":
            case "c":
            case "d":
            case "e":
            case "f":
            case "g":
            case "h":
                System.out.println("Letra");
                break;
            case "!=":
            case "==":
            case "<":
            case "<=":
            case ">":
            case ">=":
            case "=":
                System.out.println("Operador logico");
                break;
            default:
                System.out.println("não identificado");
                break;
        }
    }
}
