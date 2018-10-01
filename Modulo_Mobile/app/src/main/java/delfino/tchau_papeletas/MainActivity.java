package delfino.tchau_papeletas;

//Autores: Delfino, Voltan, Abreu
//Principal fonte de consulta: https://developer.android.com/guide/
// Fonte de consulta secundaria: https://developer.android.com/training/camera/photobasics
//Versao 1.3 -> Tirada de Foto ok, Buscar da Galeria Ok, Enviar msg Ok


import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.content.pm.PackageInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    //Configurações de conexão com o servidor
    private static String ip_teste = "192.168.1.3";
    private static String ip_rede_movel = "192.168.43.73";
    private static String ip_teste_Voltan = "192.168.43.5";
    private static String ip = ip_teste_Voltan;//"192.168.43.5";//"192.168.43.7"; //IP do Computador na tentativa de conexão estabelecida
    private static int porta = 8102;

    //Declaração das variaveis globais
    static final int Requisicao_Tirar_Foto = 1;
    private int Requisicao_Escolher_Foto = 2;
    private static Socket socket;
    private static PrintWriter printwriter;

    String codigo = "";
    String tempo_aula = "";
    String tempo_aula2 = "";
    String msg = "";
    int tempo_aula_aux = 1;
    String Foto_String = "";
    String Foto_String_check = "0";
    String check_success = "";

    ImageView Foto_Selecionada;
    Bitmap Foto_Bitmap;
    ImageButton Upload;
    String Caminho_Destino_Foto;
    EditText Creditos,Codigo;
    Spinner Tempo_aula;
    Spinner Tempo_aula2;

    public ByteArrayOutputStream stream;
    public byte[] byteFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Associação das variaveis com o activity_main
        ImageButton BotaoCamera = (ImageButton)findViewById(R.id.BotaoCamera);
        ImageButton BotaoEnvio = (ImageButton)findViewById(R.id.Upload);
        Foto_Selecionada = (ImageView)findViewById(R.id.Foto_Selecionada);
        Creditos = (EditText)findViewById(R.id.Creditos);
        Codigo = (EditText)findViewById(R.id.Codigo);
        Tempo_aula = (Spinner)findViewById(R.id.Tempo_aula);
        Tempo_aula2 = (Spinner)findViewById(R.id.Tempo_aula2);
        //Criando um array de acordo com as strings feitas para especificação dos tempos de aula
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tempos_de_aula, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Associando o adaptador com o spinner
        Tempo_aula.setAdapter(adapter);
        Tempo_aula2.setAdapter(adapter);

        //Permitir apenas a visualização dos créditos, i.e: proibir edição
        Creditos.setEnabled(false);
        //Se o usuario não tiver camera o botao de tirar foto será desabilitado
        if(!TemCamera())
            BotaoCamera.setEnabled(false);
    }

    //Checando se o usuário tem a camera
    private boolean TemCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public class connectTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Conectando ao servidor
                socket = new Socket(ip,porta);
                printwriter = new PrintWriter(socket.getOutputStream());
                //Enviando para o servidor a foto mostrada
                printwriter.write(msg);
                printwriter.flush();

                //Fechando o servidor, apenas um envio.
                printwriter.close();
                socket.close();
                //Notificação permanente de que a imagem foi enviada onde estavam os créditos
                Creditos.setText("Arquivo de código: '" + codigo + "' enviado.");
                Foto_String = "";

            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public void Abrir_Camera(View view){
        //Abre a camera para retirar foto e mostra o resultado (foto tirada)
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, Requisicao_Tirar_Foto);
        //Salvar imagem no dispositivo: desnecessário
        if(intent.resolveActivity(getPackageManager())!=null && 0==1){
            //Cria o local de foto para onde a imagem tirada deve ir
            File arq_foto = null;
            try{
                arq_foto = Criar_Arq_Imagem();
            }catch (IOException e){
            }
            if (arq_foto!=null){
                Uri photoUri = FileProvider.getUriForFile(this,"delfs_rules",arq_foto);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(intent, Requisicao_Tirar_Foto);
            }
        }
    }


    private File Criar_Arq_Imagem()throws IOException{

        //Caminhos de destino alternativos
        //File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File path_SD = Environment.getExternalStorageDirectory();

        //Criando nome do arquivo
        //Data e hora
        String timeStamp  = new SimpleDateFormat("ddMMyy_HHmmss").format(new Date());
        //Caso se queira adicionar algo depois
        String nome_arq = "Tchau_Papeletas_"+ timeStamp;
        //Diretório a ser salvo
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                nome_arq,  /* prefixo */
                ".jpg",         /* sufixo */
                storageDir      /* diretório */
        );

        //Caminho para ser usado nos outros intents
        Caminho_Destino_Foto = image.getAbsolutePath();
        return image;
    }


    public void Abrir_Galeria(View view){
        //Abre a camera para retirar foto e mostra o resultado (foto tirada)
        Intent intent = new Intent();
        // Mostrando apenas imagens
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecone a foto"), Requisicao_Escolher_Foto);
    }

    public void Fazer_Upload(View view){
        //Checando se o código foi inserido/é válido
        codigo = Codigo.getText().toString();
        tempo_aula = (String) Tempo_aula.getSelectedItem().toString();
        tempo_aula2 = (String) Tempo_aula2.getSelectedItem().toString();
        Foto_Selecionada = findViewById(R.id.Foto_Selecionada);
        Foto_Selecionada.buildDrawingCache();
        Foto_Bitmap = null;
        Foto_Bitmap = Foto_Selecionada.getDrawingCache();
        stream = null;
        stream = new ByteArrayOutputStream();
        Foto_Bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byteFormat = stream.toByteArray();
        Foto_String = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        if(codigo.length()==1){
            Toast toast = Toast.makeText(getApplicationContext(), "O código precisa ter no mínimo 2 caracteres", Toast.LENGTH_SHORT);
            toast.show();
        }else{
            if(codigo.length()>=2){
                //Checando se um tempo de aula foi inserido
                if(!tempo_aula.equals("0") && !tempo_aula2.equals("0") ){
                    //Checando se uma imagem foi upada
                    if(!Foto_String_check.equals("0")){
                        // Criando a tarefa de conexão ao servidor
                        msg = codigo + "," + tempo_aula + "," + tempo_aula2 + "," + Foto_String;
                        connectTask myTask = new connectTask();
                        myTask .execute();
                        try{
                            synchronized(this){
                                wait(1000);
                            }
                        }catch(InterruptedException e){
                            Toast toast = Toast.makeText(getApplicationContext(), "Falha na sincronização", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        check_success = Creditos.getText().toString();
                        if(check_success.contains("enviado")) {
                            //Removendo a imagem da visualização
                            Foto_Selecionada.setImageBitmap(null);
                            ImageView Foto_Exibida = (ImageView) findViewById(R.id.foto_exibida);
                            //Exibindo imagem de que o upload foi feito com sucesso
                            Foto_Exibida.setVisibility(View.VISIBLE);
                            //Desabilitando o botão de Upload, para não haver novas tentativas de envio
                            ImageButton Upload = (ImageButton) findViewById(R.id.Upload);
                            Upload.setEnabled(false);
                            //Botão ficará habilitado, para envio de uma segunda foto da turma
                           // Foto_String_check = "0";

                            //Notificação de sucesso
                            Toast toast = Toast.makeText(getApplicationContext(), "Upload feito com sucesso!", Toast.LENGTH_SHORT);
                            toast.show();
                        }else{
                            //Notificação de fracasso: falha na conexão com o servidor
                            Toast toast = Toast.makeText(getApplicationContext(), "Servidor está inacessível.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }else{
                        //Notificação de fracasso: ausência de imagem
                        Toast toast = Toast.makeText(getApplicationContext(), "Por favor, selecione uma imagem.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }else{
                    //Notificação de fracasso: ausência de tempo de aula
                    Toast toast = Toast.makeText(getApplicationContext(), "Por favor, selecione os tempos de aula.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }else{
                Toast toast = Toast.makeText(getApplicationContext(), "Por favor, preencha um código.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(codigo.equals("Delfs")){
            Toast toast = Toast.makeText(getApplicationContext(), "Easter egg do Delfs encontrado! xD", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_CANCELED && data != null){
            //Tirando a foto
            if (requestCode == Requisicao_Tirar_Foto) {
                Bundle extras = data.getExtras();
                Bitmap foto = (Bitmap) extras.get("data");
                Foto_Selecionada.setImageBitmap(foto);
                Foto_String_check = "1";
                Toast toast = Toast.makeText(getApplicationContext(), "Foto tirada com sucesso!", Toast.LENGTH_SHORT);
                toast.show();
            }
            //Escolhendo a foto da galeria
            if (requestCode == Requisicao_Escolher_Foto) {
                Uri uri = data.getData();
                Bitmap foto = null;
                try {
                    foto = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();Toast toast = Toast.makeText(getApplicationContext(), "Algo deu errado...", Toast.LENGTH_SHORT);
                    toast.show();
                }
                Foto_Selecionada.setImageBitmap(foto);
                Foto_String_check = "1";
                Toast toast = Toast.makeText(getApplicationContext(), "Foto selecionada com sucesso!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        setResult(1);
        super.onBackPressed();
    }

}

