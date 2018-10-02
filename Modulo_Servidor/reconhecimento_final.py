import cv2
import os
import numpy as np   #Usado para extrair valor de luminosidade da foto
import shutil #Utilizado para mover/copiar arquivos
import time
from conexao import Conecta

#Reconhecimento baseado apenas no LBPH

detectorFace = cv2.CascadeClassifier("haarcascade-frontalface-default.xml")
reconhecedor = cv2.face.LBPHFaceRecognizer_create()
classificador_olhos = cv2.CascadeClassifier("haarcascade-eye.xml")

largura, altura = 720, 720
fonte = cv2.FONT_HERSHEY_COMPLEX


#Obtendo imagens
caminhos = []
print ('Verificando pastas "apuradas" e "faltas..." \n')
if not(os.path.isdir('./apuradas')):
    os.mkdir('./apuradas') #cria-se a pasta caso nao exista
    print ('Pasta "apuradas" criada com sucesso! \n')
if not(os.path.isdir('./faltas')):
    os.mkdir('./faltas') #cria-se a pasta caso nao exista
    print ('Pasta "faltas" criada com sucesso! \n')

while(True):
    for f in os.listdir('./faltas'):
        caminhos.append(os.path.join('./faltas', f)) #o caminho das fotos estao sendo inseridos em caminhos
    print("Arquivos carregados:  " + str(caminhos))


    time.sleep(5)
    for caminhoImagem in caminhos: #estamos pegando as fotos uma a uma que foram inseridas na lista caminhos
        imagem = cv2.imread(caminhoImagem)
        contadorfoto = 1
    # teste o que foi carregado, indica-se luminosidade > 110
        print("Exibindo arquivo: " + str(caminhoImagem))
    # Conexão com o banco de dados
        conexao1 = Conecta('localhost', 'ime', 'postgres', 'admin')
    #extração de dados do nome da foto
        nome_arquivo =  caminhoImagem.split("\\")[-1]
        informacoes_foto = nome_arquivo.split(".")   #estamos extraindo turma, dia, tempo de aula e numero da foto

        a = "="
        print(40*a)
        print("Informações da foto em análise: ")
        print("Nome Turma: " + informacoes_foto[0] + " data: "+ informacoes_foto[2] + "-" + informacoes_foto[3]+ "-" + informacoes_foto[4] )
        print("Tempo de aula: "+ informacoes_foto[5] + " Disciplina: "+ informacoes_foto[1])
        print(40 * a)
        print("Pessoas identificadas:")
        nome_da_turma = informacoes_foto[0] + 'cinza'

        #Parametros para o banco de dados
        # Recebo do aplicativo nome_turma  materia  tempo_aula  dia mes ano
        nome_turma = str(informacoes_foto[0])
        materia = str(informacoes_foto[1])
        tempo_aula = str(informacoes_foto[5])
        dia = str(informacoes_foto[2])
        mes = str(informacoes_foto[3])
        ano = str(informacoes_foto[4])
        caminho = nome_arquivo    #nome que auxilia à encontrar as imagens
        cod_aula = conexao1.inserir_aula(nome_turma,  materia,  tempo_aula,  dia, mes, ano, caminho)
        #Procurando treinamento da turma
        treinamento_lbph = 'aprendiLBPH' + nome_da_turma + '.yml'
        if (os.path.isfile(treinamento_lbph)):  #if else, se encontrei => identifico as faces, do contrário printo "Não existe treinamento para essa turma!"
            reconhecedor.read(treinamento_lbph)
            imagem_cinza = cv2.cvtColor(imagem,cv2.COLOR_BGR2GRAY)  # a imagem em tom cinza, pois melhora detecção da face
            face_detec = detectorFace.detectMultiScale(imagem_cinza, scaleFactor=1.5, minSize=(100, 100))
            # detecta  face
            for(x, y, l, a) in face_detec:
                regiao = imagem[y:y + a, x:x + l]
                olhos_cinzas = cv2.cvtColor(regiao, cv2.COLOR_BGR2GRAY)
                olhos_detectados = classificador_olhos.detectMultiScale(olhos_cinzas, scaleFactor=1.5, minSize=(10, 10))
                # detecta olho na face
                for (x2, y2, l2, a2) in olhos_detectados:
                    imagem_face = cv2.resize(imagem_cinza[y:y + a, x:x + l], (largura, altura))
                    cv2.rectangle(imagem, (x, y), (x + l, y + a), (255, 255, 255), 4) #255 200 16 para azul
                    matricula, distancia = reconhecedor.predict(imagem_face)
                    if matricula== -1:
                        matricula = 000   #desconhecido, usou--se matricula 0
                    print(" lbph: " + str(distancia) + " matr" + str(matricula))
                    #Atualizar a presença de false para true
                    conexao1.atualizar_presenca(cod_aula, str(matricula))
                    cv2.putText(imagem, str(matricula), (x+5, y + (a + 50)), fonte, 1, (255, 255, 255)) #Blue-Green-Red
        #cv2.imshow("Face", imagem)
        #nome_foto = caminhoImagem.replace(informacoes_foto[0], "apurada."+informacoes_foto[0])
                cv2.imwrite(caminhoImagem, imagem)
                sair = cv2.waitKey(30) & 0xff
                if (sair == 27): break
            print("Todas as faces da imagem foram analisadas!")

            while (os.path.isfile("./apuradas/" + informacoes_foto[0]+ "."+ informacoes_foto[1]+ "."+ informacoes_foto[2]+ "."+ informacoes_foto[3]+ "."+ informacoes_foto[4]+ "."+ informacoes_foto[5]+ "." + str(contadorfoto)+"."+ informacoes_foto[6])):
                contadorfoto+=1

            nome_arquivo2 = nome_arquivo.replace(informacoes_foto[-1], str(contadorfoto)+"."+ informacoes_foto[-1])

            print("Novo nome do arquivo: " + nome_arquivo2)
            try:
                shutil.copy2(caminhoImagem, "./apuradas/"+nome_arquivo2)
            except IOError:
                print("Problema ao mover arquivo!")
            if(os.path.isfile("./apuradas/"+nome_arquivo2)):
                os.remove(caminhoImagem)
                print("Arquivo analisado e removendo da pasta faltas!")
                time.sleep(1)
                while(nome_arquivo in os.listdir('./faltas/')):
                    time.sleep(2)
                    print("Aguardando remoção completa...")
                caminhos.remove(caminhoImagem)
        else: print("Não existe treinamento para essa turma!")
        conexao1.encerrar()
    caminhos.clear()
    f = ''

