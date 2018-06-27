package main.service;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.model.FileMetadata;
import main.model.KeyMetadata;
import org.apache.commons.lang3.ArrayUtils;
import main.util.DataUtils;
import main.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class EncoderService {

    public EncoderService(){}

    public byte[] encrypt(byte[] key, byte[] plaintext, int offsetMeta, int offset){
        byte[] ciphertext = new byte[plaintext.length];
        for(int i = 0; i < plaintext.length - offsetMeta; i++){
            ciphertext[i] = (byte)(plaintext[i + offsetMeta] ^ key[i + offset]);
        }
        return ciphertext;
    }

    public int encode(String key, String plainext, String destination, KeyMetadata keyMetadata) throws IOException {
        File keyFile = new File(key);
        File plaintextFile = new File(plainext);
        int length = plaintextFile.getName().length();
        if(plaintextFile.length() + Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME +
                 length + keyMetadata.getOffset() > keyFile.length()){
            return 0;
        }

        FileMetadata fileMetadata = new FileMetadata();
        byte[] keyBits;
        byte[] plaintextBits;
        byte[] ciphertext;
        ;
        plaintextBits = DataUtils.readBitsFormFile(plainext);

        String fileName = new File(plainext).getName();
        fileMetadata.setFileName(fileName);
        fileMetadata.setSize(new File(plainext).length());

        keyBits = DataUtils.readKeyBits(key, (int) keyMetadata.getOffset() + 8, (int) (Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME + length + plaintextFile.length()));
        ciphertext = this.encrypt(keyBits, plaintextBits, 0, Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME + length);
        String hash = DataUtils.hash(ciphertext);
        fileMetadata.setHash(hash);
        byte[] sizeBytes = DataUtils.longToBytes(fileMetadata.getSize());

        byte[] hashBytes = hash.getBytes();
        byte[] filenameBytes = fileMetadata.getFileName().getBytes();
        fileMetadata.setMetaSize(Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME + length);
        byte[] metaSizeBytes = DataUtils.longToBytes(fileMetadata.getMetaSize());

        byte[] id = new byte[8];
        for(int i = 0; i < 8; i++){
            id[i] = keyBits[i];
        }
        byte[] idBytes = id;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(sizeBytes);
        outputStream.write(metaSizeBytes);
        outputStream.write(hashBytes);
        outputStream.write(filenameBytes);

        byte metadata[] = outputStream.toByteArray( );
        outputStream.reset();

        byte[] encMetadata = encrypt(keyBits, metadata,0, idBytes.length);
        outputStream.write(idBytes);
        outputStream.write(encMetadata);
        byte[] toWrite = outputStream.toByteArray();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(destination + "/" +
                    fileMetadata.getFileName().substring(0, fileMetadata.getFileName().length() - 3) + "enc"));
            fileOutputStream.write(ArrayUtils.addAll(toWrite, ciphertext), 0, toWrite.length + ciphertext.length);
            fileOutputStream.flush();
            fileOutputStream.close();
            keyMetadata.setOffset(keyMetadata.getOffset() + Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME + length + plaintextFile.length() + 8);
            Files.deleteIfExists(Paths.get(plainext));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataUtils.writeKeyMetadata(Util.KEY_METADATA_PATH, keyMetadata);
        return length;
    }
}
