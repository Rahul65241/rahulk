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

import java.net.*; //Socket
import java.io.*;  //PrintWriter|BufferedReader|InputStreamReader|IOException

/**
 * La classe {@code Client} rappresenta uno user del programma, riceve comandi di protocollo per la 
 * comunicazione dal server e quando consentito invia i suoi comandi del programma.
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 */
public class Client {

    private static void printSafjNest() {
        System.out.println(""
                + "███████╗ █████╗ ███████╗   ██╗    ███╗   ██╗███████╗███████╗████████╗\n"
                + "██╔════╝██╔══██╗██╔════╝   ██║    ████╗  ██║██╔════╝██╔════╝╚══██╔══╝\n"
                + "███████╗███████║█████╗     ██║    ██╔██╗ ██║█████╗  ███████╗   ██║   \n"
                + "╚════██║██╔══██║██╔══╝██   ██║    ██║╚██╗██║██╔══╝  ╚════██║   ██║   \n"
                + "███████║██║  ██║██║   ╚█████╔╝    ██║ ╚████║███████╗███████║   ██║   \n"
                + "╚══════╝╚═╝  ╚═╝╚═╝    ╚════╝     ╚═╝  ╚═══╝╚══════╝╚══════╝   ╚═╝   ");
    }

    /**
     * Si connette al server, genera le chiavi per l'RSA
     * poi rispetta i comandi di protocollo del server.
     * @param args Argomenti della linea di comando.
     */
    public static void main(String[] args) {
        String hostName = "localhost";
        int portNumber = 65535;
        KeyGenerator generator = new KeyGenerator(); //Generatore di chiavi RSA
        KeyPair clientPair;     //Paio di chiavi RSA del client
        int maxChars = 0;       //Massimo numero di caratteri inviabili
        String toSend = null;   //Stringa da mandare al server
        String response = null; //Stringa della risposta del server
        String key = null;      //Chiave pubblica dell'ultima persona a cui si e' mandato un messaggio

        printSafjNest();

        try (
            Socket echoSocket = new Socket(hostName, portNumber);                                       //Si connette al Server
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);                      //Scrive nel Buffer del Server
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())); //Legge il Buffer del Client
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));                //Input da tastiera
        ) {
            System.out.println("Generazioni delle chiavi RSA in corso...");
            clientPair = generator.generateKeys(2048); //Genera le chiavi a bit specificati
            maxChars = RSA.maxChars(clientPair.getPublicKey());

            //Ciclo in cui il client risponde ai comandi di protocollo di comunicazione del server
            //Continua finche' la risposta del server non è QUIT o nulla
            while ((response = in.readLine()) != null && !response.equals("QUIT")) {
                //Il server vuole ricevere un input dall'utente 
                if(response.equals("INPUT")) {   
                    System.out.print("\r\n>");
                    toSend = stdIn.readLine();
                    out.println(toSend);
                }

                //Il server vuole ricevere un comando in input dall'utente
                else if(response.equals("INPUTC")) {
                    //Input da tastiera
                    System.out.print("\r\n>");
                    toSend = stdIn.readLine();

                    //Se l'utente ha scritto il comando send ci sono un po' di controlli che il client 
                    //deve fare prima di mandare il messaggio al server
                    //Innanzitutto controlla se lo user a cui si vuole mandare un messaggio esiste
                    //chiedendo la chiave pubblica dello user al server e vedendo la sua risposta
                    //Se la sua risposta non e' "<Server> sintassi errata" allora controlla se il messaggio e' 
                    //abbastanza corto per essere criptato, se si lo invia
                    if(toSend.indexOf("send ") == 0) {
                        String toSendArray[] = toSend.split(" ", 3);
                        if(toSendArray.length == 3) {
                            out.println("getkey " + toSendArray[1]);
                            key = in.readLine();
                            in.readLine();
                            if(key.equals("<Server> username non trovato")) {
                                System.out.println("Username non trovato");
                                out.println("send ");
                            }
                            else if(toSendArray[2].length() <= maxChars) {
                                toSendArray[2] = RSA.encrypt(toSendArray[2], key);
                                toSend = String.join(" ", toSendArray);
                                out.println(toSend);
                            }
                            else {
                                System.out.println("Il messaggio non puo' superare gli/i " + maxChars + " caratteri");
                                out.println("send ");
                            }
                        }
                        else
                            out.println(toSend);
                    }
                    //Il resto dei comandi
                    else
                        out.println(toSend);
                }

                //Il server avvisa il client che sta per arrivare un messaggio criptato
                //Quindi lo legge e lo decripta
                //Il messaggio che arriva e' in formato "[data/ora]<ricevente> contenuto criptato"
                else if(response.equals("DECRYPT")) {
                    response = in.readLine();
                    String responseArray[] = response.split(" ", 2);
                    responseArray[1] = RSA.decrypt(responseArray[1], clientPair.getPrivateKey());
                    response = String.join(" ", responseArray);
                    System.out.println(response);
                }

                //Il server vuole ricevere la chiave pubblica del client
                else if(response.equals("SENDKEY")) {
                    out.println(clientPair.getPublicKey());
                }

                //Risposte del server che non sono comandi del protocollo
                else {
                    System.out.println(response);
                }
            }
        } catch (UnknownHostException e) { //Non riesce a connettersi al server
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) { //Altri problemi di connessione
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
