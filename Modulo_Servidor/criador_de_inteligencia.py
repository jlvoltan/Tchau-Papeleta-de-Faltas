
import cv2
import os
import numpy as np

codigo_turma=0 #Usaremos na verificação da pasta
#Reconhecimento baseado apenas o LBPH

lbph = cv2.face.LBPHFaceRecognizer_create(radius=1, neighbors=8, grid_x=12, grid_y=12,threshold=50) #1.7976931348623157e+100


def getFotoMatricula( pasta):  #O parametro turma se refere ao código da turma que é o mesmo que o nome da pasta
    lista_face = []
    lista_matricula = []

    caminhos = [os.path.join(pasta,foto ) for foto in os.listdir(pasta)]
    for f in os.listdir(pasta):
        caminhos.append(os.path.join(pasta, f))

    for caminhoFoto in caminhos:
        imagem_face = cv2.cvtColor(cv2.imread(caminhoFoto), cv2.COLOR_BGR2GRAY)
        matricula = int(caminhoFoto.split('.')[1])
        lista_face.append(imagem_face)
        lista_matricula.append(matricula)
    return np.array(lista_matricula), lista_face

#Obtendo o nome da turma a ser aprendida com o usuário
while(codigo_turma == 0):
    turma = input("Digite o nome da turma que deseja realizar o aprendizado: ")
    nome_da_turma_cinza = turma + 'cinza'      #Salvamos na pasta  turma concatenada com a palavra cinza
    if(os.path.isdir(nome_da_turma_cinza)):
        codigo_turma = 1
    else:
        print("O nome da turma informado deve ser o mesmo nome da pasta, onde se encontram as fotos a serem convertidas!\n")
        print("Por exemplo, digite turma1, caso as fotos convertidas estejam na pasta turma1cinza \n")
matriculas, faces = getFotoMatricula(nome_da_turma_cinza)

print("Imagens e matrículas da " + nome_da_turma_cinza + " carregadas com sucesso")
print("Aprendendo...")

lbph.train(faces, matriculas)
lbph.write('aprendiLBPH' + nome_da_turma_cinza + '.yml')

print("treinamento da turma " + turma + " concluído.")