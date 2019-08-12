package com.stocks;

import com.stocks.BankActor.Bank;
import com.stocks.BankActor.Account;
import com.stocks.MarketActor.SaleTransaction;

import java.io.Serializable;
import java.util.*;  
import com.fasterxml.jackson.databind.ObjectMapper;

public interface BankMessages {

    class ActionPerformed implements Serializable {
        private final String description;

        public ActionPerformed(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    class GetBalance implements Serializable {
    }

    class CreateAccount implements Serializable{
        private final Account account;

        public CreateAccount(Account account){
            this.account = account;
        }

        public Account getAccount(){
            return account;
        }
    }

    class DoTransaction implements Serializable{
        private final SaleTransaction t;
        public DoTransaction(SaleTransaction t){
            this.t=t;
        }

        public SaleTransaction getTransaction(){
            return t;
        }
    }

    class AddBalance implements Serializable{
        private final int id;
        private final int value;

        public AddBalance(){
            id=0;
            value=0;
        }
        public AddBalance(int id,int value){
            this.id=id;
            this.value = value;
        }

        public int getId(){
            return id;
        }
        public int getValue(){
            return value;
        }
    }
}