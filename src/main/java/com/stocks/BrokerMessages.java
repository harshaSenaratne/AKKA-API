package com.stocks;

import com.stocks.UserRegistryActor.User;

import java.io.Serializable;
import java.util.*;  
import com.fasterxml.jackson.databind.ObjectMapper;

public interface BrokerMessages {

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }


    class Buy implements Serializable {

    }
    class Sell implements Serializable {

    }
    class GetSales implements Serializable{

    }
    class GetStockValues implements Serializable{
        
    }
}