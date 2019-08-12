package com.stocks;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;

import java.util.*;

public class AnalystActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static class Analyst {

    public Analyst() {
    }

  }


  static Props props() {
    return Props.create(AnalystActor.class);
  }

  private final Analyst analyst = new Analyst();

  @Override
  public Receive createReceive(){
    return receiveBuilder()
            .match(AnalystMessages.GetTip.class, getTip -> {

            })
            .matchAny(o -> log.info("received unknown message"))
            .build();
  }
}
