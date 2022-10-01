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
import java.io.*;         //PrintWriter|BufferedReader|InputStreamReader|IOException
import java.util.HashMap; //Mappe

/**
 * La classe {@code ServerThread} rappresenta un thread di esecuzione di {@link Server}.
 * Contiene il riferimento alla {@code MessageBox} e alla Mappa dei comandi di {@code Server}.
 * <p>
 * Il thread esegue il log in di uno user utilizzando solo uno userName, prende la chiave pubblica del client e aggiunge lo user alla {@code MessageBox}.
 * Il programma poi entra in un ciclo di risposta ai comandi inviati dal client che finisce con il comando {@code quit}.
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 */
public class ServerThread extends Thread{
    private Socket s;                            //Socket con la connessione ad un client
    private MessageBox mBox;                     //MessageBox contenente mappe di messaggi e chiavi pubbliche per ogni User       
    private HashMap<String, String> commandList; //Mappa delle descrizione dei comandi

    private String userName = null;
    private String clientKey = null;
    
    /**
     * Costruttore di {@code ServerThread}.
     * @param s Un {@code Socket} con la connessione ad un client.
     * @param mBox Una {@code MessageBox} contenente messaggi e chiavi pubbliche degli user.
     * @param commandList Una {@code HashMap} contenente i comandi che il client puo' utilizzare con le loro descrizioni.
     */
    public ServerThread(Socket s, MessageBox mBox, HashMap<String, String> commandList) {
        this.s = s;
        this.mBox = mBox;
        this.commandList = commandList;
    }

    /**
     * La parte del server che legge e risponde ad un client.
     * <p>
     * Esegue il log in utilizzando solo uno userName, prende la chiave pubblica del client e aggiunge lo user alla {@code MessageBox}.
     * Poi il programma entra in un ciclo di risposta ai comandi inviati dal client che finisce quando il client si disconnette.
     * <p>
     * Il protocollo per la comunicazione tra server e client usato prevede che all'invio di specifici comandi 
     * da parte del server, il client risponda in un certo modo.
     * I comandi sono:
     * <ul>
     *    <li>INPUT - Chiede al client di inviargli un input dell'utente</li>
     *    <li>INPUTC - Chiede al client di inviargli un comando</li>
     *    <li>DECRYPT - Segnala al client che il prossimo messaggio che gli verra' inviato sara' criptato</li>
     *    <li>SENDKEY - Chiede al client di inviargli la sua chiave pubblica</li>
     * </ul>
     */
    public void run() {
        String line = null;

        try(
            PrintWriter out = new PrintWriter(s.getOutputStream(), true); //Scrive nel Buffer del Client
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); //Legge il Buffer del Server
        ) {
            //Procedura di log in, con solo uno username
            //Lo username deve essere senza spazi e non già presente nel Server
            do {                                                                        
                out.println("<Server> Scegli lo username (no spazi): ");
                out.println("INPUT");
                userName = in.readLine();
            }while(userName.contains(" ") || mBox.contains(userName) || userName.equals("Server"));

            //Chiede al client di mandargli la sua chiave pubblica
            out.println("SENDKEY");
            clientKey = in.readLine();

            //Aggiunge lo user (userName e chiave pubblica) alla MessageBox completando la procedura di log in
            mBox.newUser(userName, clientKey);

            //Invia il benvenuto all'utente
            System.out.println(userName + " connected");
            out.println("<Server> Benvenuto " + userName);
            out.println("Digitare help per aiuto");
            out.println("INPUTC");

            //Ciclo in cui il client invia un comando e il server lo compie e in caso manda una risposta al client
            //Termina quando riceve null ovvero quando l'utente si disconnette
            while((line = in.readLine()) != null) {
                String lineArray[] = line.split(" ", 3); //Divide la stringa inviata dal client ad ogni spazio per massimo 3 volte
                System.out.println(userName + " -> " + lineArray[0]); //Stampa comando sul server

                //Controlla se il comando immesso dallo user esiste, se esiste lo esegue
                switch (lineArray[0].toLowerCase()) {
                    //Invia al client la lista degli utenti online
                    case "list":
                        out.println(mBox.listUsers());
                    break;

                    //Invia il messaggio al suo destinatario
                    case "send":
                    if(lineArray.length < 3) { //Se l'utente non ha scritto il comando nel formato [comando destinatario messaggio]    
                        out.println("<Server> sintassi errata");
                    }
                    else if(!mBox.send(lineArray[1], userName, lineArray[2])) { //Se non riesce ad inviare il messaggio
                        out.println("<Server> si e' verificato un errore con l'invio");
                    }
                    break;

                    //Invia al client i messaggi ricevuti
                    case "receive":
                        if(mBox.hasMessageFor(userName)) { //Se ci sono messaggi per quello user
                            //Finchè ci sono messaggi per quello user invia al client l'ultimo messaggio ricevuto dal corrispetivo utente
                            do {            
                                out.println("DECRYPT");
                                out.println(mBox.getLastMessageFor(userName).getFormattedMessage());
                            }while(mBox.hasMessageFor(userName));
                        }
                        else
                            out.println("<Server> nessun nuovo messaggio :(");
                    break;

                    //Invia al client la chiave pubblica dell'utente richiesto
                    case "getkey":
                        if(lineArray.length < 2) { //Se l'utente non ha scritto nel formato [comando username]  
                            out.println("<Server> sintassi errata");
                        }
                        else if(mBox.getKey(lineArray[1]) != null) { //Se esiste lo username del quale si richiede la chiave
                            out.println(mBox.getKey(lineArray[1])); //Invia la chiave
                        }
                        else {
                            out.println("<Server> username non trovato");
                        }
                    break;

                    //Invia al client la guida dei comandi
                    case "help":
                        if(lineArray.length < 2) { //Se l'utente ha inserito solo help
                            out.println("Per ulteriori informazioni su uno specifico comando, digitare HELP nome comando.\r\n"
                                    + "LIST\tVisualizza la lista degli utenti online\r\n"
                                    + "SEND\tInvia un messaggio criptato alla persona indicata\r\n"
                                    + "RECEIVE\tScrive i messaggi indirizzati a te\r\n"
                                    + "GETKEY\tScrive la chiave dell'utente specificato\r\n"
                                    + "QUIT\tEsce dal programma\r\n"
                                    + "HELP\tFornisce la guida per i comandi");
                        }
                        else if (commandList.get(lineArray[1]) != null) { //Se il comando esiste
                            out.println(commandList.get(lineArray[1].toLowerCase())); //Prende la descrizione del comando inserito
                        }
                        else {
                            out.println("Comando non supportato dalla utilità di Guida");
                        }
                    break;

                    //Esce dal programma
                    case "quit":
                        out.println("sei sicuro? (s/n)");
                        out.println("INPUT");
                        if(in.readLine().charAt(0) == 's') //Se la risposta inizia con s
                            out.println("QUIT");
                    break;

                    //Se non esiste il comando inserito
                    default:
                        out.println("<Server> comando non trovato");
                    break;
                }
                out.println("INPUTC");      
            }
        } catch(IOException e) { //Problemi di connessione, probabilmente il client che si scollega
            System.out.println(userName + ": Exception caught when trying to listen on port " + s.getPort() + " or listening for a connection");
            System.out.println(userName + ": " + e.getMessage());
        }

        //Quando il client si disconnette rimuove le informazioni dell'utente e termina il thread
        System.out.println(userName + " disconnected");
        mBox.removeUser(userName);
        Server.close(this);
    }
}
