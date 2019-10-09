package ru.MyCloud.common;

import java.nio.file.Path;

public class PackageFile extends AbstractMessage {
    private String fileName; //Имя передаваемого файла
    private int numberPackage; // номер пакета
    private boolean lastPackage; //признак последнего пакета передаваемого файла
    private byte[] dataPackage; // байтовый массив пакета файла

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getNumberPackage() {
        return numberPackage;
    }

    public void setNumberPackage(int numberPackage) {
        this.numberPackage = numberPackage;
    }

    public boolean isLastPackage() {
        return lastPackage;
    }

    public void setLastPackage(boolean lastPackage) {
        this.lastPackage = lastPackage;
    }

    public byte[] getDataPackage() {
        return dataPackage;
    }

    public void setDataPackage(byte[] dataPackage) {
        this.dataPackage = dataPackage;
    }

    public PackageFile(Path path, int numberPackage, boolean lastPackage, byte[] dataPackage) {
        this.fileName = path.getFileName().toString();
        this.numberPackage = numberPackage;
        this.lastPackage = lastPackage;
        this.dataPackage = dataPackage;
    }
}
