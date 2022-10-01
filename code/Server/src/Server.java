/**
 * MIT License
 *
 * Copyright (c) 2021 Leonardo Panichi

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

import java.net.*;        //Socket
import java.util.HashMap; //Mappe
import java.util.ArrayList;
import java.io.IOException;

/**
 * La classe {@code Server} serve ad intercettare le connessioni dei client.
 * ed avviare un {@link ServerThread} quando la connessione viene accettata.
 * <p>
 * Vengono memorizzati i thread avviati dal server, la mappa dei messaggi e la mappa delle descrizioni dei comandi che possono essere eseguiti dal client.
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 */
public class Server {
    private static ArrayList<ServerThread> threads = new ArrayList<ServerThread>();     //Arraylist di thread
    private static MessageBox mBox = new MessageBox();                                  //MessageBox contenente mappe di messaggi e chiavi pubbliche per ogni User
    private static HashMap<String, String> commandList = new HashMap<String, String>(); //Mappa delle descrizioni dei comandi che possono essere eseguiti dal client
                                                                                        //L'indice e' il nome del comando, l'argomento e' la descrizione del comando

    /**
     * Rimuove un istanza del {@link ServerThread} dall' {@link Server#threads Arraylist dei thread}.
     * @param s istanza di {@link ServerThread} da rimuovere dall'{@link Server#threads Arraylist dei thread}.
     */
    public static void close(ServerThread s) {
        threads.remove(s);
    }

    /**
     * Crea il socket del server e accetta le richieste di connessione.
     * @param args Argomenti della linea di comando.
     * @throws IOException Errori di connessione socket.
     */
    public static void main(String[] args) throws IOException {
        int portNumber = 65535;

        //Aggiunge le descrizioni dei comandi disponibili alla mappa dei comandi
        commandList.put("list", "Visualizza la lista dei possibili riceventi\r\n\r\nLIST");
        commandList.put("send", "Visualizza la lista degli utenti online\r\n\r\nSEND [destinatario] [messaggio]\r\n\r\n\tdestinatario - username di un utente online\r\n\tmessaggio - messaggio da inviare");
        commandList.put("receive", "Scrive i messaggi indirizzati a te\r\n\r\nRECEIVE");
        commandList.put("getkey", "Scrive la chiave pubblica dell'utente specificato\r\n\r\nGETKEY");
        commandList.put("quit", "Esce dal programma\r\n\r\nQUIT");
        commandList.put("help", "Fornisce la guida per i comandi\r\n\r\nHELP [comando]\r\n\r\n\tcomando - visualizza informazioni di guida per il comando.");
        
        System.out.println("Server started");
        while(true) {
            try (
                ServerSocket serverSocket = new ServerSocket(portNumber); //Crea il socket del server
            ) {
                Socket s = serverSocket.accept();
                threads.add(new ServerThread(s, mBox, commandList)); //Aggiunge il thread alla lista
                threads.get(threads.size()-1).start(); //Avvia l'ultimo thread
            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
                System.out.println(e.getMessage());
                break;
            }
        }
    }
}
