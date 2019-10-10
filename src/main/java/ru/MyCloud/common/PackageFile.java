package ru.MyCloud.common;

import ru.MyCloud.common.Message.AbstractMessage;

import java.nio.file.Path;

public class PackageFile extends AbstractMessage {
    private String fileName; //��� ������������� �����
    private int numberPackage; // ����� ������
    private boolean lastPackage; //������� ���������� ������ ������������� �����
    private byte[] dataPackage; // �������� ������ ������ �����

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
