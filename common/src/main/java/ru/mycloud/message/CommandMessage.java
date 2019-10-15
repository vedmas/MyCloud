package ru.mycloud.message;

public class CommandMessage extends AbstractMessage {
    private int numberOrder;
    private String fileName;

    public int getNumberOrder() {
        return numberOrder;
    }

    public String getFileName() {
        return fileName;
    }

    public CommandMessage(int numberOrder, String fileName) {
        this.numberOrder = numberOrder;
        this.fileName = fileName;
    }
}
