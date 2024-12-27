package com.sip.sipproject.bean;

import java.util.UUID;

public class IdGenerator {

    public String generateId() {
        UUID uuid =UUID.randomUUID();
        String id =uuid.toString();
        return id;
    }
}
