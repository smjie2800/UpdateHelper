package com.updateHelper.file;

import java.io.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.regex.Pattern;

import sun.misc.*;
public class ExportPrivateKey {
    private File keystoreFile;
    private String keyStoreType;
    private char[] password;
    private String alias;
    private File exportedFile;
    public static KeyPair getPrivateKey(KeyStore keystore, String alias, char[] password) {
        try {
            Key key=keystore.getKey(alias,password);
            if(key instanceof PrivateKey) {
                Certificate cert=keystore.getCertificate(alias);
                PublicKey publicKey=cert.getPublicKey();
                return new KeyPair(publicKey,(PrivateKey)key);
            }
        } catch (UnrecoverableKeyException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyStoreException e) {
        }
        return null;
    }
    public void export() throws Exception{
        KeyStore keystore=KeyStore.getInstance(keyStoreType);
        BASE64Encoder encoder=new BASE64Encoder();
        keystore.load(new FileInputStream(keystoreFile),password);
        KeyPair keyPair=getPrivateKey(keystore,alias,password);
        PrivateKey privateKey=keyPair.getPrivate();
        String encoded=encoder.encode(privateKey.getEncoded());
        FileWriter fw=new FileWriter(exportedFile);
        fw.write("—–BEGIN PRIVATE KEY—–\n");
        fw.write(encoded);
        fw.write("\n");
        fw.write("—–END PRIVATE KEY—–");
        fw.close();
    }

    public static void lineBreak(String srcFilePath, String dstFilePath, int lineLength) {
        String line = "";

        try{
            InputStream is = new FileInputStream(srcFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            OutputStream out = new FileOutputStream(dstFilePath);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            String tempLine = null;
            while ((tempLine= reader.readLine()) != null) {
                line += tempLine;
                if (line.length() >= lineLength) {
                    writer.write(line.substring(0,lineLength));
                    writer.newLine();

                    line = line.substring(lineLength);
                }
            }
            while (line.length() >= lineLength) {
                writer.write(line.substring(0,lineLength));
                writer.newLine();

                line = line.substring(lineLength);
            }

            writer.write(line);
            writer.newLine();


            reader.close();
            is.close();

            writer.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    public static void main(String args[]) throws Exception{
        *//*ExportPrivateKey export=new ExportPrivateKey();
        export.keystoreFile=new File("D:/keystore.jks");
        export.keyStoreType="JKS";
        export.password="RybNLQs3eLv".toCharArray();
        export.alias="intermediate2";
        export.exportedFile=new File("D:/i1pk.txt");
        export.export();*//*

        lineBreak("D:/server1.txt", "D:/pem.txt", 64);
    }*/
}


