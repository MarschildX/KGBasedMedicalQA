package com.example.healworld.utils;

import java.util.UUID;

public class MessageUtil {

    public String generateId(){
        return Long.toString(UUID.randomUUID().getLeastSignificantBits());
    }

}
