/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tchau_papeletas_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;

/**
 *
 * @author Delfino, Voltan, Abreu
 */
public class TCPServer extends Thread {
    public static final int SERVERPORT = 8102;
    private boolean running = false;
    private PrintWriter mOut;
    private OnMessageReceived messageListener;
    public ServerSocket serverSocket;
 
    public static void main(String[] args) {
 
        //Abre a janela de exibição (camada de apresentação para o usuário)
        ServerBoard frame = new ServerBoard();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    
    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
    
    //Possível resposta para o android
    /*public void sendMessage(String message){
        if (mOut != null && !mOut.checkError()) {
            mOut.println(message);
            mOut.flush();
        }
    }*/
    boolean next = true;
    @Override
    public void run() {
        super.run();
        while(true){
            running = true;
            if(next){
                next=false;
                try {
                    System.out.println("S: Conectando...");
                    messageListener.messageReceived("Server: Conectando...\n");
                    //Criação do socket server
                    serverSocket = new ServerSocket(SERVERPORT);
                    //Cliente - aceitando conexão
                    Socket client = serverSocket.accept();
                    System.out.println("S: Recebendo...");
                    messageListener.messageReceived("Server: Conectado!\n");
                    try {
                        //Envia para o cliente
                        mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                        //Lê a mensagem do cliente
                        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        //loop infinito para receber mensagens do cliente
                        while (running) {
                            String message = in.readLine();
                            if (message != null && messageListener != null) {
                                //Chama o metodo de recebimento ao receber a mensagem
                                messageListener.messageReceived(message);
                                serverSocket.close();
                                next = true;
                                running = false;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("S: Erro");
                        e.printStackTrace();
                    } finally {
                        client.close();
                        System.out.println("S: Concluído!");
                    }
                } catch (Exception e) {
                    System.out.println("S: Erro");
                    e.printStackTrace();
                }
            }
        }
    }
 
    
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
 
}
