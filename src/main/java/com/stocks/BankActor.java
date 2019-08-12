package com.stocks;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import com.stocks.MarketActor.SaleTransaction;

import java.util.*;

public class BankActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static class Bank {
    private final List<Account> accounts;

    public Bank() {
      accounts = new ArrayList<>();
    }

    public void addAccount(Account account){
      accounts.add(account);
    }

    public List<Account> getAccounts(){
      return accounts;
    }

    public Account findAccount(int userId){
      for(Account a:accounts){
        if(a.getUserId()==userId){
          return a;
        }
      }
      return new Account();
    }

    public boolean doTransaction(SaleTransaction t){
      Account seller = findAccount(t.getSellerId());
      Account buyer = findAccount(t.getBuyerId());

      //if user accounts are wrong
      if(seller.getUserId()==0 || buyer.getUserId()==0){
        return false;
      }

      // if buyer account has low balance
      if(buyer.getBalance()<t.getValue()){
        return false;
      }

      seller.setBalance(seller.getBalance()+t.getValue());
      buyer.setBalance(buyer.getBalance()-t.getValue());

      return true;
    }

    public Account addBalance(int id,int value){
      Account acc = findAccount(id);

      if(acc.getUserId()==0){
        return new Account();
      }

      acc.setBalance(acc.getBalance()+value);
      return acc;
    }
  }

  public static class Account{
    private int balance;
    private final int userId;


    public Account(){
      this.balance = 0;
      this.userId=0;
    }

    public Account(int balance,int userId){
      this.balance = balance;
      this.userId = userId;
    }

    public int getBalance(){
      return balance;
    }

    public int getUserId(){
      return userId;
    }

    public void setBalance(int bal){
      balance = bal;
    }
  }

  static Props props() {
    return Props.create(BankActor.class);
  }

  private final Bank bank = new Bank();

  @Override
  public Receive createReceive(){
    return receiveBuilder()
            .match(BankMessages.GetBalance.class, getBalance -> {
              getSender().tell(bank, getSelf());
            })
            .match(BankMessages.DoTransaction.class, req -> {
              boolean success = bank.doTransaction(req.getTransaction());
              if(success){
                getSender().tell(req.getTransaction(),getSelf());
              }else{
                getSender().tell(new SaleTransaction(),getSelf());
              }
            })
            .match(BankMessages.CreateAccount.class, createAccount -> {
              log.info("======== creating account");
              bank.addAccount(createAccount.getAccount());
              // getSender().tell(createAccount.getAccount(), getSelf());
            })
            .match(BankMessages.AddBalance.class, acc -> {
              Account res = bank.addBalance(acc.getId(),-acc.getValue());
              getSender().tell(res, getSelf());
            })
            .matchAny(o -> log.info("received unknown message"))
            .build();
  }
}
