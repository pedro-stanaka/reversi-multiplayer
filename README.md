# Reversi Multiplayer

Jogo Reversi implementado em JAVA, apresentado como trabalho de disciplina de Redes no curso de Ciência da Computação (UEL).

Este jogo foi implementado utilizando a API padrão do Java para sockets TCP.


## Compilando e rodando a aplicação

Para jogar é preciso compilar/rodar o servidor primeiro:
```bash
cd server
mvn clean install assembly:single
java -jar target/reversi-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

E depois o cliente:
```bash
cd client
mvn clean install assembly:single
java -jar target/reversi-client-1.0-SNAPSHOT-jar-with-dependencies.jar
```
