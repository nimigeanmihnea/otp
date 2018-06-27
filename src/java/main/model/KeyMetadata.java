package main.model;

public class KeyMetadata {

    private long id;
    private long size;
    private long meta_size;
    private long offset;

    public KeyMetadata(){}


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

    public long getMeta_size() {
        return meta_size;
    }

    public void setMeta_size(long meta_size) {
        this.meta_size = meta_size;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
