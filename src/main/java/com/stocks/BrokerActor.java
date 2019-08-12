package com.stocks;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;

import java.util.*;

public class BrokerActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static class Broker {
    
    public Broker() {

    }

  }


  static Props props() {
    return Props.create(BrokerActor.class);
  }

  private final Broker broker = new Broker();

  @Override
  public Receive createReceive(){
    return receiveBuilder()
            .match(MarketMessages.Buy.class, buy->{
              
            })
            .matchAny(o -> log.info("received unknown message"))
            .build();
  }
}
