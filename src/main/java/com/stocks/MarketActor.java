package com.stocks;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class MarketActor extends AbstractActor {

  ActorRef bankActor;
  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  //#user-case-classes
  public static class Market {
    private final List<Sale> buys;
    private final List<Company> companies;
    private final List<Sale> sales;

    public Market() {
        Random rand = new Random(); 

        this.companies = new ArrayList<>();
        Company c1 = new Company(1,"Hayleys",rand.nextInt(100)+1);
        Company c2 = new Company(2,"Sampath Bank",rand.nextInt(100)+1);
        Company c3 = new Company(3,"Singer",rand.nextInt(100)+1);
        Company c4 = new Company(4,"NTB",rand.nextInt(100)+1);
        Company c5 = new Company(5,"John Keells",rand.nextInt(100)+1);
        Company c6 = new Company(6,"Cargills",rand.nextInt(100)+1);
        Company c7 = new Company(7,"Seylan Bank",rand.nextInt(100)+1);
        Company c8 = new Company(8,"Highland",rand.nextInt(100)+1);
        Company c9 = new Company(9,"Coca Cola",rand.nextInt(100)+1);
        Company c10 = new Company(10,"Elephant House",rand.nextInt(100)+1);

        companies.add(c1);
        companies.add(c2);
        companies.add(c3);
        companies.add(c4);
        companies.add(c5);
        companies.add(c6);
        companies.add(c7);
        companies.add(c8);
        companies.add(c9);
        companies.add(c10);

        this.sales = new ArrayList<>();
        this.buys = new ArrayList<>();

    }

    public List<Company> getCompanies(){
        return companies;
    }

    public Sale addSale(Sale sale){
      boolean exists = false;

      for(Sale s:sales){
        if(s.getCompanyId()==sale.getCompanyId() && s.getUserId() == sale.getUserId()){
          exists=true;
          s.setValue(s.getValue()+sale.getValue());
          return s;
        }
      }

      if(!exists){
        sales.add(sale);
        return sale;
      }

      return new Sale();
    }

    public List<Sale> getSales(){
      return sales;
    }

    public boolean removeSale(SaleTransaction t){
      boolean contains=false;
      for(Sale s:sales){
        if(s.getCompanyId()==t.getCompanyId() && s.getUserId() == t.getSellerId()){
          contains=true;
          if(s.getValue()<t.getValue()){
            return false;
          }else{
            s.setValue(s.getValue()-t.getValue());
          }
        }
      }
      if(contains){
        return true;
      }else{
        return false;
      }
    }

    public boolean doSale(SaleTransaction t){
      if(removeSale(t)){
        return true;
      }else{
        return false;
      }

    }

    public void changeCompanyValues(){
      Random rand = new Random(); 
      for(Company c:companies){
        c.setValue(rand.nextInt(100)+1);
      }
    }

    public void addBuy(Sale sale){
        buys.add(sale);
    }

    public List<Sale> getBuys(){
      return buys;
    }
  }

  public static class Company{
    private final int id;
    private final String name;
    private int stockValue;

    public Company(int id,String name,int stockValue){
      this.name = name;
      this.stockValue = stockValue;
      this.id=id;
    }

    public String getName(){
      return name;
    }

    public int getStockValue(){
      return stockValue;
    }

    public int getId(){
      return id;
    }

    public void setValue(int value){
      stockValue = value;
    }
  }

  public static class Sale{
    private final int companyId;
    private final int userId;
    private int value;

    public Sale(){
      this.userId=0;
      this.companyId=0;
      this.value=0;
    }

    public Sale(int companyId,int userId,int value){
      this.userId=userId;
      this.companyId=companyId;
      this.value=value;
    }

    public int getUserId(){
      return userId;
    }

    public int getCompanyId(){
      return companyId;
    }

    public int getValue(){
      return value;
    }

    public void setValue(int value){
      this.value = value;
    }
  }

  public static class SaleTransaction{
    private final int buyerId;
    private final int companyId;
    private final int sellerId;
    private final int value;

    public SaleTransaction(){
      this.sellerId = 0;
      this.buyerId = 0;
      this.value = 0;
      this.companyId = 0;
    }

    public SaleTransaction(int buyerId,int companyId,int sellerId,int value){
      this.sellerId = sellerId;
      this.buyerId = buyerId;
      this.value = value;
      this.companyId = companyId;
    }

    public int getBuyerId(){
      return buyerId;
    }
    public int getSellerId(){
      return sellerId;
    }
    public int getValue(){
      return value;
    }
    public int getCompanyId(){
      return companyId;
    }
  }

  static Props props() {
    return Props.create(MarketActor.class);
  }

  private final Market market = new Market();

  @Override
  public Receive createReceive(){
    return receiveBuilder()
            .match(MarketMessages.SetActors.class, actors -> {
              log.info(">>> setting actor refs in MarketActor");
              bankActor = actors.getBankActor();
            })
            .match(MarketMessages.GetCompanies.class, getCompanies -> {
              getSender().tell(market, getSelf());
            })
            .match(MarketMessages.AddSale.class, addSale -> {

              Sale sale = addSale.getSale();  
              Sale modifiedSale = market.addSale(sale);
              getSender().tell(modifiedSale,getSelf());

            })
            .match(MarketMessages.Buy.class, buys -> {

              market.addBuy(buys.getBuy());
              
            })
            .match(MarketMessages.BuySale.class, buy -> {

              SaleTransaction t = buy.getTransaction();  
              boolean exists = false;


              //removing sale from sales list
              exists = market.removeSale(t);
              market.addBuy(new Sale(t.getCompanyId(),t.getBuyerId(),t.getValue()));
              //reducing bank balance of user
              
            })
            .match(MarketMessages.ChangeCompanyValues.class, req -> {

                log.info("### changing company values ###");
                market.changeCompanyValues();
                getSender().tell(market, getSelf());
            })
            .matchAny(o -> log.info("received unknown message"))
            .build();
  }
}
