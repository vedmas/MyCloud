package ru.mycloud;

import ru.mycloud.message.AbstractMessage;

import java.nio.file.Path;

public class PackageFile extends AbstractMessage {
    private String fileName;
    private boolean lastPackage;
    private byte[] dataPackage;

    public String getFileName() {
        return fileName;
    }

    public boolean isLastPackage() {
        return lastPackage;
    }

    public byte[] getDataPackage() {
        return dataPackage;
    }

   public PackageFile(Path path, boolean lastPackage, byte[] dataPackage) {
        this.fileName = path.getFileName().toString();
        this.lastPackage = lastPackage;
        this.dataPackage = dataPackage;
    }
}
