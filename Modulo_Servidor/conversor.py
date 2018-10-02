import cv2
import os
import numpy as np   #Usado para extrair valor de luminosidade da foto

#Classificadores que estamos usando, o de face e o de olhos
classificador = cv2.CascadeClassifier("haarcascade-frontalface-default.xml")
classificador_olhos = cv2.CascadeClassifier("haarcascade-eye.xml")

#tamanho deve ser igual em todas fotos
altura, largura = 720, 720


#Obtendo imagens
caminhos = []
matricula_lista = []
n_fotos_matricula = []
n_faces_matricula = []
nome_turma=0
i = j = k = 0    #contadores, de matricula, fotos e faces

opcao1 = 3
print("Seja bem-vindo ao conversor de imagens \n")
while(nome_turma == 0):
    turma = input("Digite o nome da turma que deseja carregar para conversão: ")
    if(os.path.isdir(turma)):
        nome_turma = 1
    else:
        print("O nome da turma informado deve ser o mesmo nome da pasta, onde se encontram as fotos a serem convertidas!\n")
turma_cinza = turma + "cinza"


#Precisamos verificar se a pasta turmacinza já existe ou se deve ser criada
if not(os.path.isdir(turma_cinza)):
    os.mkdir(turma_cinza) #cria-se a pasta caso nao exista
    print ('Pasta criada com sucesso! \n')


while(opcao1 != 1 and opcao1 != 0):
    opcao1 = int(input("Deseja exibir a fotografia que esta sendo carregada? (0- Não  1 - Sim)     "))

for f in os.listdir(turma):
    caminhos.append(os.path.join(turma, f))

#print("Arquivos carregados:  " + str(caminhos))

print("\n\nESTATISTICAS: ")

for caminho_imagem in caminhos:
    if not matricula_lista:    #Testando se a lista esta vazia
        matricula_lista.append(caminho_imagem.split(".")[1])
        print("Matricula: " + matricula_lista[i])
        j = 1

    elif (matricula_lista[-1] != caminho_imagem.split(".")[1]):
        matricula_lista.append(caminho_imagem.split(".")[1])
        n_fotos_matricula.append(j)
        n_faces_matricula.append(k)
        print(" de " + str(n_fotos_matricula[-1]) + " fotos, foram reconhecidas " + str(n_faces_matricula[-1]) + " faces.")
        print("======================================================")
        i += 1
        j = 1
        k = 0
        print("Matricula: " + matricula_lista[i])

    else: j += 1  #Pois temos mais uma foto para a mesma matrícula

    imagem = cv2.imread(caminho_imagem)

    #Abrir janela com a foto que está convertendo
    if(opcao1 == 1):
        print("Clique em fechar a imagem para continuar.")
        cv2.namedWindow("saida", cv2.WINDOW_FULLSCREEN)
        imag_tela = cv2.resize(imagem, (960, 640))
        cv2.imshow("saida", imag_tela)
        cv2.waitKey(0)

    imagem_cinza = cv2.cvtColor(imagem, cv2.COLOR_BGR2GRAY) # a imagem em tom cinza, pois melhora detecção da face
    face_detec = classificador.detectMultiScale(imagem_cinza, scaleFactor=1.5, minSize=(100, 100))

    # teste o que foi carregado, indica-se luminosidade > 110
    print("Exibindo arquivo: " + str(caminho_imagem) + " luminosidade: " + str(np.average(imagem_cinza)))

    for(x, y, l, a) in face_detec:  # detecta  face
        regiao = imagem[y:y + a, x:x + l]
        olhos_cinzas = cv2.cvtColor(regiao, cv2.COLOR_BGR2GRAY)
        olhos_detectados = classificador_olhos.detectMultiScale(olhos_cinzas, scaleFactor=1.5, minSize=(10, 10))
        for (x2, y2, l2, a2) in olhos_detectados:         #detecta olho na face
            imagem_face = cv2.resize(imagem_cinza[y:y + a, x:x + l], (largura, altura))
            nome_foto = caminho_imagem.replace(turma, turma_cinza)
            cv2.imwrite(nome_foto, imagem_face)
            k += 1
            break
        break

#Caso especial da ultima matricula que não tem seguinte para se comparar
n_fotos_matricula.append(j)
n_faces_matricula.append(k)
print(" de " + str(n_fotos_matricula[-1]) + " fotos, foram reconhecidas " + str(n_faces_matricula[-1]) + " faces.")