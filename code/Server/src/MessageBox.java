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

import java.util.HashMap;       //Mappe
import java.util.LinkedList;    //Struttura dati LinkedList
import java.util.Queue;         //Code
import java.time.LocalDateTime; //Data e tempo

/**
 * La classe {@code MessageBox} rappresenta una Message Box, memorizzando la Messagge Box vera e propia come una {@code HashMap} 
 * con una {@code Queue} di {@code Message} come argomento e memorizzando la lista delle chiavi pubbliche come una {@code HashMap}.
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 */
public class MessageBox {
    private HashMap<String, Queue<Message>> mb = new HashMap<String, Queue<Message>>(); //Mappa con userName del ricevente come indice 
                                                                                        //e coda di messaggi come argomento
    private HashMap<String, String> pk = new HashMap<String, String>();                 //Mappa con userName dello user come indice e 
                                                                                        //chiave pubblica come argomento

    /**
     * Inserisce un nuovo user nella message box, aprendo uno spazio per i messaggi che gli verranno inviati. 
     * Inserisce anche la sua chiave pubblica nella mappa di chiavi pubbliche.
     * <p>
     * Questo metodo <b>e'</b> sincronizzato.
     * @param userName UserName dello user.
     * @param publicKey Chiave pubblica dello user.
     */
    public synchronized void newUser(String userName, String publicKey) {
        mb.put(userName, new LinkedList<>()); //Inizializza, nella mappa dei messaggi, la coda dei messaggi ricevuti dello user
        pk.put(userName, publicKey);          //Inserisce, nella mappa delle chiavi, la chiave pubblica dello user
    }

    /**
     * Rimuove uno user dalla mappa dei messaggi e dalla mappa delle chiavi pubbliche.
     * <p>
     * Questo metodo <b>e'</b> sincronizzato.
     * @param userName UserName dello user.
     */
    public synchronized void removeUser(String userName) {
        if(mb.containsKey(userName)) {
            mb.remove(userName);
            pk.remove(userName);
        }
        else {
            System.out.println("problema");
        }
    }

    /**
     * Controlla se uno user ha dei messaggi da ricevere.
     * <p>
     * Questo metodo <b>non e'</b> sincronizzato.
     * @param userName UserName dello user.
     * @return {@code true} se {@code userName} ha dei messaggi da ricevere.
     */
    public boolean hasMessageFor(String userName) {
        if((!mb.containsKey(userName)) || (mb.get(userName).isEmpty())) //Se il destinatario non e' presente, o la sua coda di messaggi e' vuota
            return false;
        return true;
    }

    /**
     * Legge ed elimina l'ultimo messaggio dalla coda dei messaggi ricevuti dallo user.
     * <p>
     * Questo metodo <b>e'</b> sincronizzato.
     * @param userName UserName dello user.
     * @return L'ultimo messaggio nella cosa di {@code userName}.
     */
    public synchronized Message getLastMessageFor(String userName) {
        return mb.get(userName).poll();
    }

    /**
     * Prende la rappresentazione in stringa della lista degli utenti online.
     * <blockquote><pre>
     *    [userName1, userName2, ...]
     * </pre></blockquote>
     * <p>
     * Questo metodo <b>non e'</b> sincronizzato.
     * @return Lista degli utenti online.
     */
    public String listUsers() {
        return mb.keySet().toString();
    }

    /**
     * Aggiunge un messaggio alla lista del ricevitore.
     * <p>
     * Questo metodo <b>e'</b> sincronizzato.
     * @param receiver Ricevente del messaggio.
     * @param sender Mandante del messaggio.
     * @param msg Contenuto del messaggio.
     * @return {@code true} se l'invio e' andato a buon fine.
     */
    public synchronized boolean send(String receiver, String sender, String msg) {
        if(!mb.containsKey(receiver)) { //Se non è presente il ricevente a cui si fa riferimento
            return false;
        }
        mb.get(receiver).add(new Message(sender, msg, LocalDateTime.now())); //Aggiunge un nuovo messaggio alla coda dei messaggi del ricevente
        return true;
    }
    
    /**
     * Controlla se esiste gia' un utente nella mappa con lo stesso username.
     * <p>
     * Questo metodo <b>non e'</b> sincronizzato.
     * @param userName UserName che si vuole controllare.
     * @return {@code true} se esiste gia' quello userName.
     */
    public boolean contains(String userName) {
        if(mb.containsKey(userName)) //Se esiste gia' un utente nella mappa con lo stesso username
            return true;
        return false;
    }

    /**
     * Ottiene la chiave pubblica di {@code userName}.
     * <p>
     * Questo metodo <b>non e'</b> sincronizzato.
     * @param userName userName la quale chiave pubblica si vuole ottenere.
     * @return La chiave pubblica di {@code userName}.
     */
    public String getKey(String userName) {
        return pk.get(userName);
    }
}
