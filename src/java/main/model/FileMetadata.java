package main.model;

import java.io.Serializable;

public class FileMetadata implements Serializable{

    private long id;
    private long size;
    private long metaSize;
    private String hash;
    private String fileName;


    public FileMetadata() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getMetaSize() {
        return metaSize;
    }

    public void setMetaSize(long meta_size) {
        this.metaSize = meta_size;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id=" + id +
                ", size=" + size +
                ", metaSize=" + metaSize +
                ", hash='" + hash + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
