package ru.MyCloud.common;

public class OrderMessage extends AbstractMessage {
    private int numberOrder;

    public int getNumberOrder() {
        return numberOrder;
    }

    public OrderMessage(int numberOrder) {
        this.numberOrder = numberOrder;
    }
}
