package main.service;

import main.model.KeyMetadata;
import main.util.DataUtils;
import main.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class DefragmentService {

    public DefragmentService(){}

    public void defrag(byte[] key, KeyMetadata keyMetadata) {
        int j = 0, count = 0, toWriteStart = 0, finalDefragment = 0;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(key.length);

        for (int i = 0; i < key.length; i++) {
            if (key[i] == Util.ZERO_FLAG[j]) {
                count++;
            } else {
                j = -1;
                count = 0;
            }
            j++;
            if (count == 16) {
                byte[] start = new byte[8];
                byte[] length = new byte[8];

                for (int k = i + 1; k < i + 9; k++) {
                    start[k - i - 1] = key[k];
                }
                for (int l = i + 9; l < i + 17; l++) {
                    length[l - i - 9] = key[l];
                }
                long startAsLong = DataUtils.byteToLong(start, 0, 8);
                long lengthAsLong = DataUtils.byteToLong(length, 0, 8);
                if (toWriteStart + 8 < startAsLong)
                    stream.write(key, toWriteStart, (int) startAsLong);
                toWriteStart = (int) (startAsLong + lengthAsLong);
                i = (int) (startAsLong + (int) DataUtils.byteToLong(length, 0, 8));
                j = 0;
                count = 0;
                finalDefragment = (int) (startAsLong + lengthAsLong);
            }
        }
        if (finalDefragment < key.length)
            stream.write(key, finalDefragment, key.length - finalDefragment);
        byte[] streamByte = stream.toByteArray();

        try {
            DataUtils.generateKey(Util.KEY_PATH.substring(0, Util.KEY_PATH.length() - 4) + ".enc1", "64", "16");
            byte[] keyStream = DataUtils.readKeyBits(Util.KEY_PATH.substring(0, Util.KEY_PATH.length() - 4) + ".enc1", 0,
                    (int) new File(Util.KEY_PATH.substring(0, Util.KEY_PATH.length() - 4) + ".enc1").length());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(Util.KEY_PATH));
            if(streamByte != null)
                fileOutputStream.write(ArrayUtils.addAll(streamByte, keyStream), 0, streamByte.length + keyStream.length);
            else
                fileOutputStream.write(keyStream, 0, keyStream.length);
            fileOutputStream.flush();
            fileOutputStream.close();
            keyMetadata.setOffset(streamByte.length - 8);
            DataUtils.writeKeyMetadata(Util.KEY_METADATA_PATH, keyMetadata);
            Files.deleteIfExists(Paths.get(Util.KEY_PATH.substring(0, Util.KEY_PATH.length() - 4) + ".enc1"));
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void defragment(String key, int stop, KeyMetadata keyMetadata){
        byte[] keyBytes = DataUtils.readKeyBits(key,0, stop);
        defrag(keyBytes, keyMetadata);
    }
}
