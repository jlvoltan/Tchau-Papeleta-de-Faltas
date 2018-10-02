import psycopg2

class Conecta(object):
    _banco = None
    def __init__(self, mhost, database, usuario, senha):
        self._banco = psycopg2.connect(host=mhost, database=database, user=usuario, password=senha)

    def comandar(self, comandoSQL):  #Serve para inserir, alterar ou excluir
        try:
            cursor = self._banco.cursor()
            cursor.execute(comandoSQL)
            self._banco.commit()
            cursor.close()
        except:
            return 0
        return 1

    def consultar(self, consultaSQL):
        resultado = None
        try:
            cursor = self._banco.cursor()
            cursor.execute(consultaSQL)
            resultado = cursor.fetchall()
        except:
            return None
        return resultado


    def encerrar(self):
        self._banco.close()

    def inserir_presenca(self,cod_aula, matricula):
        cod_papeleta = cod_aula + "_" + matricula
        comandoSQL = "INSERT INTO tchau_papeleta.papeleta VALUES('" + cod_papeleta + "', '" + cod_aula + "', '" + matricula +"', " + "false);"
        if (self.comandar(comandoSQL) != 1) :
            print('Falha ao inserir papeleta!')

    def atualizar_presenca(self,cod_aula, matricula):
        cod_papeleta = cod_aula + "_" + matricula
        comandoSQL = "UPDATE tchau_papeleta.papeleta SET presente = true WHERE cod_papeleta = '" + cod_papeleta +  "';"
        if (self.comandar(comandoSQL) != 1):
            print('Falha ao atualizar papeleta!')

    def inserir_aula(self,nome_turma,  materia,  tempo_aula,  dia, mes, ano, caminho):
        comandoSQL1 = "SELECT cod_disciplina FROM tchau_papeleta.disciplina WHERE nome_turma = '" + nome_turma + "' AND materia = '" + materia + "';"
        resultado = self.consultar(comandoSQL1)
        if not resultado:
            print("A aula indicada não pertence a essa turma!!")
            return 0
        cod_disciplina = str(resultado[0][0])
        cod_aula = dia + mes + ano + "_" + cod_disciplina + "_" + tempo_aula
        # 1º verificar se a aula já existe
        comandoSQL2 = "SELECT tempo_aula FROM tchau_papeleta.aula WHERE cod_aula = '" + cod_aula + "';"
        resultado2 = self.consultar(comandoSQL2)
        if not resultado2:   #neste caso a lista está vazia, temos de inserir
            comandoSQL3 = "INSERT INTO tchau_papeleta.aula VALUES('" + cod_aula + "', '" + dia + "', '" + mes + "', '" + ano + "', '" + cod_disciplina + "', '" + tempo_aula + "', '" + caminho + "');"
            if (self.comandar(comandoSQL3) != 1):
                print('Falha ao inserir aula!')
                return 0
            comandoSQL4 = "SELECT matricula FROM tchau_papeleta.aluno WHERE nome_turma = '" + nome_turma + "';"
            resultado3 = self.consultar(comandoSQL4)
            for matricula in resultado3:   #Se está colocando falta para todos os alunos ("setando")
                self.inserir_presenca(cod_aula, matricula[0])
        return cod_aula




