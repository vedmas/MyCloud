package ru.MyCloud.common;

import ru.MyCloud.common.Message.AbstractMessage;

import java.nio.file.Path;

public class PackageFile extends AbstractMessage {
    private String fileName; //Имя передаваемого файла
    private int numberPackage; // номер пакета
    private boolean lastPackage; //признак последнего пакета передаваемого файла
    private byte[] dataPackage; // байтовый массив пакета файла

    public String getFileName() {
        return fileName;
    }

    public boolean isLastPackage() {
        return lastPackage;
    }

    byte[] getDataPackage() {
        return dataPackage;
    }

    PackageFile(Path path, int numberPackage, boolean lastPackage, byte[] dataPackage) {
        this.fileName = path.getFileName().toString();
        this.numberPackage = numberPackage;
        this.lastPackage = lastPackage;
        this.dataPackage = dataPackage;
    }
}
