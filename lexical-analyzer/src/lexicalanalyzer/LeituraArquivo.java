/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexicalanalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Victor Souza e Pedro Brandão
 */
public class LeituraArquivo {
    
    private String localFile;
    private int num;
    
    public ArrayList<String> leitura(){
    
        ArrayList<String> code = new ArrayList<>();
        File access = new File("test/input/");
      
        for(File aux : access.listFiles()){
            if(aux.getName().contains("entrada")){ // Verificação do nome do arquivo
                code.add(aux.getName());
            }
            else{
               System.out.println("Nome do arquivo fora do padrão: " + aux.getName());
               System.out.println("");
            }
        }
        return code;
    }
    
    public ArrayList<String> lerArquivo(String localFile) throws FileNotFoundException{
    
        ArrayList<String> code;
        try (Scanner scanner = new Scanner(new FileReader("test/input/" + localFile))) {
            this.localFile = localFile;
            this.num= num+1;
            code = new ArrayList<>();
            while(scanner.hasNextLine()){
                String aux = scanner.nextLine();
                if(aux.length() != 0){
                    code.add(aux);
                }
            }
        }
        return code;
        
    }
    
    public void escreverArquivo(ArrayList<Token> tokens, ArrayList<String> erros) throws IOException{
        try (FileWriter file = new FileWriter("test/exit/" + "saida" + this.num + ".txt", false)) {
            PrintWriter gravar = new PrintWriter(file);
            tokens.forEach((token) -> {
                gravar.println("Linha: " + token.getLinha() + " | " + "Lexema: " + token.getLexema() + " | " + "Tipo: " + token.getTipo());
            });
            if(erros.isEmpty())
                gravar.println("\n Nao existem erros léxicos");
            else{
                erros.forEach((erro) -> {
                    gravar.println("ERRO: " + erro);
                });
            }
        }
    
    }
    
    public String getLocalFile() {
        return localFile;
    }
    
    
}
