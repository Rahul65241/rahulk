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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Base64; //Codifica e decodifica base64

/**
 * Nella classe {@code RSA} sono presenti i metodi per criptare e decriptare stringhe con l'algoritmo RSA.
 * <p>
 * Inoltre e' presente una funzione per controllare, partendo da una chiave, il massimo numero di caratteri 
 * che una stringa puo' contenere per essere criptabile con l'algoritmo RSA.
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 * @see <a href="https://it.wikipedia.org/wiki/RSA_(crittografia)">Wikipedia: RSA</a>
 */
public class RSA {
    /** 
     * Partendo dalla chiave con cui si deve criptare il messaggio, ritorna il massimo numero di caratteri 
     * in una stringa per essere criptabile con l'RSA.
     * @param key La chiave di cui si vuole controllare il numero massimo di caratteri criptabili.
     * @return Il numero massimo di caratteri criptabili.
    */
    public static int maxChars(String key){
        BigInteger modulus = new BigInteger(new String(Base64.getDecoder().decode(key.substring(key.indexOf("-") + 1)))); //Decifra e divide n dal resto 
                                                                                                                          //della chiave
        return (modulus.bitLength() / 8) - 1; //Calcola il numero massimo di byte e quindi di caratteri che si possono criptare
    }
    
    /**
     * Cripta una stringa con l'algoritmo RSA usando una chiave.
     * @param message Il messaggio da criptare.
     * @param key La chiave con cui si vuole criptare il messaggio.
     * @return Il messaggio criptato, codificato in Base64.
     * @throws ArithmeticException se {@code message} convertito in intero e' un numero piu' grande del {@code modulo - 1} preso da {@code key}.
     */
    public static String encrypt(String message, String key){
        String[] keyArray = key.split("-"); //Spezza la chiave in esponente e modulo

        //Decodifica esponente e modulo da Base64 a BigInteger
        BigInteger exponent = new BigInteger(new String(Base64.getDecoder().decode(keyArray[0])));
        BigInteger modulus = new BigInteger(new String(Base64.getDecoder().decode(keyArray[1])));

        //Converte la stringa message in un array di bytes dove sono concatenate le rappresentazioni binarie dei singoli caratteri della stringa
        byte[] bytes = null;
        try {
            bytes = message.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        BigInteger IntMessage = new BigInteger(bytes); //Converte l'array di byte in un numero, ovvero assume che nell'array di bytes ci sia un numero 
                                                       //e lo converte nella sua rappresentazione in base 10
        
        if(IntMessage.compareTo(modulus.subtract(BigInteger.ONE)) >= 0) //Se il numero dato dalla conversione da stringa di message 
                                                                        //e' piu' grande di N-1, ovvero del modulo usato per l'RSA, 
                                                                        //l'algoritmo non puo' funzionare quindi lancia un eccezione
            throw new ArithmeticException("Message too long for key lenght");

        BigInteger IntEncryptedMessage = IntMessage.modPow(exponent, modulus); //criptazione RSA
        String encryptedMessageBase64 = Base64.getEncoder().encodeToString(IntEncryptedMessage.toString().getBytes()); //codifica il numero 
                                                                                                                       //criptato in Base64
        return encryptedMessageBase64;
    }

    /**
     * Decripta una stringa criptata con l'RSA e codificata in base 64 usando una chiave.
     * @param encryptedMessageBase64 Il messaggio criptato con l'RSA e codificato in base 64.
     * @param key La chiave con cui si vuole decriptare il messaggio.
     * @return Il messaggio decriptato.
     */
    public static String decrypt(String encryptedMessageBase64, String key){
        String[] keyArray = key.split("-"); //Spezza la chiave in esponente e modulo

        //Decodifica esponente e modulo da Base64 a BigInteger
        BigInteger exponent = new BigInteger(new String(Base64.getDecoder().decode(keyArray[0])));
        BigInteger modulus = new BigInteger(new String(Base64.getDecoder().decode(keyArray[1])));

        BigInteger IntEncryptedMessage = new BigInteger(new String(Base64.getDecoder().decode(encryptedMessageBase64))); //Decodifica il numero criptato
                                                                                                                         //assumendo sia in base64
        BigInteger IntMessage = IntEncryptedMessage.modPow(exponent, modulus); //decriptazione RSA

        byte[] bytes = IntMessage.toByteArray(); //Converte il messaggio in un array di bytes

        String message = null;
        //Converte ogni Byte dell'array in caratteri usando come codifica l'UTF-8 e li unisce in una stringa
        try {
            message = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return message;
    }
}
