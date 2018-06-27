package main.service;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import main.model.FileMetadata;
import main.model.KeyMetadata;
import main.util.DataUtils;
import main.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DecoderService {

    private KeyMetadata keyMetadata;

    public DecoderService(){}

    public boolean decrypt(byte[] key, byte[] ciphertext, String destination, int length, String path, ArrayList<Rectangle> decList) {
        byte[] id = new byte[8];
        int index = 0, count = 0, j = 0;

        for (int i = 0; i < 8; i++) {
            id[i] = ciphertext[i];
        }

        for (int i = 0; i < key.length; i++) {
            if (key[i] == id[j]) {
                count++;
            } else {
                j = -1;
                count = 0;
            }
            j++;
            if (count == 8) {
                index = i + 1;
                break;
            }
        }

        if(index != 0) {
            byte[] metadata = new byte[Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME + length - 8];
            int aux = 0;
            for (int i = index; i < index + Util.FILE_METADATA_SIZE_WITHOUT_FILE_NAME + length - 8; i++) {
                metadata[aux] = (byte) (ciphertext[aux + 8] ^ key[i]);
                aux++;
            }

            FileMetadata fileMetadata = DataUtils.readFileMetadata(metadata);
            aux = 0;
            byte[] encData = new byte[(int) (ciphertext.length - fileMetadata.getMetaSize())];
            for (int i = (int) (index + fileMetadata.getMetaSize() - 8); i < index + ciphertext.length - 8; i++) {
                encData[aux] = ciphertext[(int) fileMetadata.getMetaSize() + aux];
                aux++;
            }
            String hash = DataUtils.hash(encData);

            if (hash.equals(fileMetadata.getHash())) {
                aux = 0;
                byte[] data = new byte[(int) (ciphertext.length - fileMetadata.getMetaSize())];
                for (int i = (int) (index + fileMetadata.getMetaSize() - 8); i < index + ciphertext.length - 8; i++) {
                    data[aux] = (byte) (key[i] ^ ciphertext[(int) fileMetadata.getMetaSize() + aux]);
                    aux++;
                }

                RandomAccessFile keyFile = null;
                try {
                    keyFile = new RandomAccessFile(new File(path), "rws");
                    keyFile.seek(index - 8);
                    keyFile.write(Util.ZERO_FLAG, 0, 16);
                    byte[] indexBytes = DataUtils.longToBytes(index - 8);
                    keyFile.write(indexBytes);
                    byte[] cipherBytes = DataUtils.longToBytes(ciphertext.length);
                    keyFile.write(cipherBytes);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    FileOutputStream outputStream = new FileOutputStream(new File(destination + "/" + fileMetadata.getFileName()));
                    outputStream.write(data, 0, data.length);
                    outputStream.flush();
                    outputStream.close();
                    keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
                    int width = calculateWidth(ciphertext.length);
                    Rectangle rectangle = new Rectangle(37 + calculateStart(index) - 8, 36, width + 8, 23);
                    decList.add(rectangle);
                    return true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean decode(String key, String ciphertext, String destination, int stop, ArrayList<Rectangle> decList){
        byte[] keyBits;
        byte[] ciphertextBits;

        ciphertextBits = DataUtils.readBitsFormFile(ciphertext);
        keyBits = DataUtils.readKeyBits(key, 8, stop);
        boolean success = decrypt(keyBits, ciphertextBits, destination, new File(ciphertext).getName().length(), key, decList);
        if(success){
            try {
                Files.deleteIfExists(Paths.get(ciphertext));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public int calculateWidth(int x){
        return (775 * x)/1000;
    }

    public double calculateStart(int x){
        return (x * 775) / 1000;
    }
}
