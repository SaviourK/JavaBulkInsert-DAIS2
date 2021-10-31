package com.kanok.inserter.model;

public class Image {

    private final byte[] data;
    private final String fileName;
    private final String fileType;

    public Image(byte[] data, String fileName, String fileType) {
        this.data = data;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }
}
