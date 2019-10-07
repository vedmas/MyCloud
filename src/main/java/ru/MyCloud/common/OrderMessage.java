package ru.MyCloud.common;

public class OrderMessage extends AbstractMessage {
    private int numberOrder;
    private String fileName;

    public int getNumberOrder() {
        return numberOrder;
    }

    public String getFileName() {
        return fileName;
    }

    public OrderMessage(int numberOrder, String fileName) {
        this.numberOrder = numberOrder;
        this.fileName = fileName;
    }
}
