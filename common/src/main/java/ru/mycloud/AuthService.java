package ru.mycloud;



import ru.mycloud.message.AuthMessage;

import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private List<AuthMessage> list = new ArrayList<>();

    private void createList() {
        list.add(new AuthMessage("Client1", "Client1"));
        list.add(new AuthMessage("Client2", "Client2"));
        list.add(new AuthMessage("Client3", "Client3"));
    }

    public boolean isAuthorization (AuthMessage msg) {
        createList();
        for (AuthMessage authMessage : list) {
            if(authMessage.getLogin().equals(msg.getLogin()) && authMessage.getPassword().equals(msg.getPassword())) {
                return true;
            }
        }
        return false;
    }
}
