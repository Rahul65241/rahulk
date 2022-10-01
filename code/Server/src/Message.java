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

import java.time.LocalDateTime; //Data e tempo
import java.time.format.DateTimeFormatter; //Formattazione di data e tempo

/**
 * La classe {@code Message} rappresenta un messaggio, memorizzandone il contenuto criptato, 
 * lo userName del mandante e la data e ora del momento di invio.
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 */
public class Message {
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm"); //Formattatore di data e ora
                                                                          //ofpattern (HH:mm) = formatta in HH:mm

    private String encryptedMsg;       //Messaggio criptato
    private String sender;             //Mandante
    private LocalDateTime sendingDate; //Data e tempo del momento di invio
    
    /**
     * Costruttore di {@code Message}.
     * @param sender Mandante del messaggio.
     * @param msg Messaggio criptato.
     * @param sendingDate Data e tempo del momento di invio.
     */
    public Message(String sender, String msg, LocalDateTime sendingDate) {
        this.encryptedMsg = msg;
        this.sender = sender;
        this.sendingDate = sendingDate;
    }

    public String getMsg() {
        return encryptedMsg;
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getSendingDate() {
        return sendingDate;
    }

    public void setMsg(String msg) {
        this.encryptedMsg = msg;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSendingDate(LocalDateTime sendingDate) {
        this.sendingDate = sendingDate;
    }
    
    /**
     * Formatta il messaggio in:
     * <blockquote>
     *    [HH:mm]&#60;mandante.> Messaggio criptato
     * </blockquote>
     * @return Il messaggio formattato.
     */
    public String getFormattedMessage(){
        return "[" + dtf.format(sendingDate) + "]" + "<" + sender + "> " + encryptedMsg;
    }
}
