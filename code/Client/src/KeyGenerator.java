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

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

/**
 * La classe {@code KeyGenerator} contiene un generatore di chiavi per l'algoritmo RSA.
 * <p>
 * Le chiavi sono rappresentate come un {@link KeyPair}. Ogni chiave viene memorizzata come:
 * <blockquote><pre>
 *    chiave = esponenteInBase64 + "-" + moduloInBase64;
 * </pre></blockquote>
 * @author <a href="https://github.com/Leon412">Leonardo Panichi</a>
 * @author <a href="https://github.com/sebastianomazzaferro">Sebastiano Mazzaferro</a>
 * @author <a href="https://github.com/adrianopesaresi">Adriano Pesaresi</a>
 */
public class KeyGenerator {

    /**
     * Genera un numero {@code BigInteger} casuale a {@code numBits} bits.
     * Piu' specificatamente tra {@code 2^(numBits - 1)} e {@code 2^(numBits)}.
     * @param numBits Numero di bit del numero casuale.
     * @return un numero casuale a {@code numBits} bits.
     */
    private static BigInteger getRandomBigIntegerBits(int numBits) {
        numBits--;
        int numBytes = (int)(((long)numBits+7)/8);
        byte[] randomBits = new byte[numBytes];
        if (numBytes > 0) {
            try {
                SecureRandom.getInstanceStrong().nextBytes(randomBits); //riempie l'array di bytes casuali
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            int excessBits = 8*numBytes - numBits;
            randomBits[0] &= (1 << (8-excessBits)) - 1;
        }
        return new BigInteger(1, randomBits).add(BigInteger.TWO.pow(numBits));
    }

    /**
     * <b>Not implemented</b> <i>Questo metodo e' stato rimpiazzato nella generazione delle chiavi da {@code getRandomBigIntegerBits} 
     * che permette di scegliere il numero di bit invece del numero di cifre del numero.</i>
     * <p>
     * Costruisce usando {@code StringBuilder} un numero {@code BigInteger} casuale di {@code digits} cifre, 
     * generando una cifra alla volta e appendendole.
     * @param digits Numero di cifre del numero.
     * @param rnd Sorgente random.
     * @return Un numero {@code BigInteger} casuale di {@code digits} cifre.
     */
    public BigInteger getRandomBigInteger(int digits, Random rnd) {
        StringBuilder sb = new StringBuilder(digits); //Costrutture di stringhe con grandezza digits
        sb.append((char)('0' + rnd.nextInt(9) + 1));  //Genera un numero casuale da 1 a 9 
                                                      //(il primo non può essere zero altrimenti il numero finale avrebbe 
                                                      //meno cifre di quelle specificate) 
                                                      //e lo appende alla stringa
        digits--;

        //Continua a generare numeri casuali (da 0 a 9 questa volta) e ad appenderli 
        //alla stringa fino a che non arriva al numero di cifre specificato
        for(int i = 0; i < digits; i++)                         
            sb.append((char)('0' + rnd.nextInt(10))); 
        return new BigInteger(sb.toString());
    }
    
    /**
     * Trova il numero primo maggiore e piu' vicino a {@code number} usando il piccolo teorema di Fermat per verificare la primalita'.
     * @param number Il numero di cui si vuole trovare il numero primo maggiore piu' vicino ad esso.
     * @return Il numero primo maggiore e piu' vicino a {@code number}.
     * @see <a href="https://it.wikipedia.org/wiki/Piccolo_teorema_di_Fermat">Wikipedia: Piccolo teorema di Fermat</a>
     */
    public BigInteger getFirstPrime(BigInteger number) {
        if(number.remainder(BigInteger.TWO) == BigInteger.ZERO)    
            number = number.add(BigInteger.ONE);
        while(BigInteger.TWO.modPow(number, number).compareTo(BigInteger.TWO) != 0)
            number = number.add(BigInteger.TWO);
        return number;
    }

    /**
     * Genera una chiave pubblica e una privata per essere usati nella criptazione e decriptazione con l'algoritmo RSA.
     * @param numBits Numero di bit del modulo delle chiavi. Assume numBits maggiore di 1. Meglio se 16 o maggiore e pari.
     * @return Un {@link KeyPair} con chiave pubblica e privata con modulo a {@code numBits} bits.
     * @see {@link RSA}
     */
    public KeyPair generateKeys(int numBits) {
        numBits = numBits / 2;
        BigInteger p = getFirstPrime(getRandomBigIntegerBits(numBits)); //Numero primo p
        BigInteger q = getFirstPrime(getRandomBigIntegerBits(numBits)); //Numero primo q
        BigInteger e = getFirstPrime(getRandomBigIntegerBits(numBits)); //Esponente pubblico | più piccolo di N e primo rispetto a z
        BigInteger n = p.multiply(q); //Modulo | N = p * q
        BigInteger z = (p.subtract(BigInteger.ONE)).multiply((q.subtract(BigInteger.ONE))); //Funzione di Eulero di N | (p – 1) * (q – 1)
        BigInteger d = e.modInverse(z); //Esponente privato | tale che e * d –> 1mod((p – 1) * (q – 1))

        //Codifica le parti delle chiavi in Base64
        String nBase64 = Base64.getEncoder().encodeToString(n.toString().getBytes());   
        String eBase64 = Base64.getEncoder().encodeToString(e.toString().getBytes());
        String dBase64 = Base64.getEncoder().encodeToString(d.toString().getBytes());

        //Unisce le parti delle chiavi con un "-"
        String publicKey = eBase64 + "-" + nBase64;
        String privateKey = dBase64 + "-" + nBase64;
        
        return new KeyPair(publicKey, privateKey);
    }
}
