package com.stocks;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;

import java.util.*;

public class ClockActor extends AbstractActor {

  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static class Clock {
    private long time;

    public Clock() {
      this.time = System.currentTimeMillis();
    }

    public Clock(long time) {
      this.time = time;
    }

    public long getTime() {
      return (System.currentTimeMillis()-time)/1000;
    }

    public void setTime(long time){
      this.time = time;
    }
  }


  static Props props() {
    return Props.create(ClockActor.class);
  }

  private final Clock clock = new Clock();

  @Override
  public Receive createReceive(){
    return receiveBuilder()
            .match(ClockMessages.GetTime.class, getTime -> {
              getSender().tell(clock, getSelf());
            })
            .match(ClockMessages.ResetTime.class, reset -> {
              clock.setTime(System.currentTimeMillis());
              getSender().tell(clock, getSelf());
            })
            .matchAny(o -> log.info("clockActor received unknown message"))
            .build();
  }
}
