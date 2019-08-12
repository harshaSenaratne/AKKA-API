package com.stocks;

import com.stocks.UserRegistryActor.User;

import java.io.Serializable;
import java.util.*;  
import com.fasterxml.jackson.databind.ObjectMapper;

public interface ClockMessages {

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    class GetTime implements Serializable {

    }

    class ResetTime implements Serializable{
        
    }
}