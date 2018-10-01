/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tchau_papeletas_server;
/**
 *
 * @author Delfino, Voltan, Abreu
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Base64;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import sun.misc.BASE64Decoder;


public class ServerBoard extends JFrame{
    private JTextArea messagesArea;
    private JButton sendButton;
    private JTextField message;
    private JButton startServer;
    private TCPServer mServer;
    private String Diretorio_fotos = "";//"";//"C:\\Users\\Gabriel\\Desktop\\";

    public ServerBoard() {

        super("Tchau Papeletas! Server");

        JPanel panelFields = new JPanel();
        panelFields.setLayout(new BoxLayout(panelFields,BoxLayout.X_AXIS));

        JPanel panelFields2 = new JPanel();
        panelFields2.setLayout(new BoxLayout(panelFields2,BoxLayout.X_AXIS));

        //Tela das mensagens
        messagesArea = new JTextArea();
        messagesArea.setColumns(25);
        messagesArea.setRows(35);
        messagesArea.setEditable(false);

        /*sendButton = new JButton("Enviar");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Recenbedo a mensagem : getText
                String messageText = message.getText();
                // Colocando a mensagem recebida em exibição
                messagesArea.append("\nVocê: " + messageText);
                // Enviando mensagem para o cliente (send message)
                mServer.sendMessage(messageText);
                //Limpando o campo
                message.setText("");
            }
        });*/

        startServer = new JButton("Iniciar serviço");
        messagesArea.append(" PFC 2018 - Delfino, Voltan, Abreu\n ");
        //messagesArea.append("Server: aguardando envio das imagens...\n ");
        startServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Após iniciar, desabilita o botão (conexao terá sido iniciada)
                startServer.setEnabled(false);

                //cria o objeto OnMessageReceived (ligado ao construtor do TCPServer)
                mServer = new TCPServer(new TCPServer.OnMessageReceived() {
                    @Override
                    public void messageReceived(String message) {
                        if(message.contains(",")){
                            String codigo,imagem,tempo_aula,tempo_aula_2, informacao_data,nome_Arq,aux_string;
                            int tempo1, tempo2;
                            String[] parts = message.split(",");
                            codigo = parts[0]; 
                            tempo_aula = parts[1];
                            tempo_aula_2 = parts[2];
                            imagem = parts[3]; 
                            informacao_data = new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime());
                            tempo1 = Integer.parseInt(tempo_aula);
                            tempo2 = Integer.parseInt(tempo_aula_2);
                            for(int aux = tempo1; aux <= tempo2; aux++){
                                aux_string = Integer.toString(aux);
                                nome_Arq= codigo + "." + informacao_data + "." + aux_string + ".jpeg";
                                messagesArea.append("\n Código: " + codigo);
                                messagesArea.append("\n Tempo de aula: " + aux_string);
                                String base64Image = imagem;
                                byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
                                try {
                                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                                    File outputfile = new File(Diretorio_fotos + nome_Arq);
                                    ImageIO.write(img, "jpeg", outputfile);
                                    messagesArea.append("\n Imagem salva.\n\nAguardando nova solicitação...\n");
                                } catch (IOException ex) {
                                    Logger.getLogger(ServerBoard.class.getName()).log(Level.SEVERE, null, ex);
                                    messagesArea.append("\n Reveja suas permissões ao programa.\n\nFalha na criação do arquivo.\n");
                                }
                            }

                            
                        }else{
                            messagesArea.append("\n "+message);
                        }
                    }
                });
                mServer.start();
            }
        });

        //Campo de digitação
        /*
        message = new JTextField();
        message.setSize(200, 20);*/

        //adicionando os campos e botões
        panelFields.add(messagesArea);
        panelFields.add(startServer);

        /*panelFields2.add(message);
        panelFields2.add(sendButton);*/

        getContentPane().add(panelFields);
        getContentPane().add(panelFields2);

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

        setSize(300, 170);
        setVisible(true);
    }
}
