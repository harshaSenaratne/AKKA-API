package com.stocks;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import com.stocks.UserRegistryActor.User;
import com.stocks.UserRegistryActor.Users;
import com.stocks.UserRegistryMessages.CreatedUser;
import com.stocks.UserRegistryActor.InitUser;
import java.util.concurrent.TimeUnit;
import java.util.*;
import java.time.Duration;
import akka.pattern.Patterns;
import java.util.concurrent.CompletionStage;
// import akka.dispatch.Await;
// import akka.dispatch.Future;
import scala.concurrent.Future;
import scala.concurrent.Await;
import com.stocks.MarketActor.Sale;

public class PlayerAIActor extends AbstractActor {
  ActorRef userRegistryActor;
  ActorRef clockActor;
  ActorRef brokerActor;
  ActorRef marketActor;
  LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public static class AIPlayers{
    private final List<User> players;

    public AIPlayers() {
      this.players = new ArrayList<>();
    }

    public AIPlayers(List<User> players) {
      this.players = players;
    }

    public List<User> getPlayers() {
      return players;
    }

    public User addAIPlayer(User user){
      players.add(user);
      return user;
    }
  }


  static Props props() {
    return Props.create(PlayerAIActor.class);
  }

  private final AIPlayers players = new AIPlayers();

  @Override
  public Receive createReceive(){
    Random rand = new Random(); 
    return receiveBuilder()
            .match(PlayerAIMessages.SetActors.class, actors -> {
              log.info(">>> setting actor refs in AIActor");
              userRegistryActor = actors.getUserRegistryActor();
              clockActor = actors.getClockActor();
              brokerActor = actors.getBrokerActor();
              marketActor = actors.getMarketActor();
            })
            .match(PlayerAIMessages.LookPlayerCompletion.class, look -> {
              TimeUnit.SECONDS.sleep(59);
              log.info("##### AI Creating players ## ");
              
              int missingCount = 0;

              Duration timeout = Duration.ofSeconds(1l);
              
              CompletionStage<UserRegistryActor.Users> getUsers = Patterns
                      .ask(userRegistryActor, new UserRegistryMessages.GetUsers(), timeout).thenApply(UserRegistryActor.Users.class::cast);

              Users users = getUsers.toCompletableFuture().get();
              int usercount = users.getUsers().size();
              missingCount = 4-usercount;

              for(int i=0;i<missingCount;i++){
                // userRegistryActor.tell(,userRegistryActor);

                CompletionStage<UserRegistryMessages.CreatedUser> createUser = Patterns
                      .ask(userRegistryActor, new UserRegistryMessages.CreateUser(new InitUser("AIPlayer"+Integer.toString(i))), timeout).thenApply(UserRegistryMessages.CreatedUser.class::cast);

                User user = createUser.toCompletableFuture().get().getUser();
                players.addAIPlayer(user);
              }
              getSelf().tell(new PlayerAIMessages.Play(),getSelf());
            })
            .match(PlayerAIMessages.Play.class, actors -> {
              int count = players.getPlayers().size();
              int selectedId = ((int)System.currentTimeMillis())%count;
              int companyId = ((int)System.currentTimeMillis())%10;
              int value = ((int)System.currentTimeMillis())%50+50;
              
              log.info("## AI playing ## "+selectedId);

              TimeUnit.SECONDS.sleep(5);
              marketActor.tell(new MarketMessages.Buy(new Sale(companyId,selectedId,value)),getSelf());
              getSender().tell(new PlayerAIMessages.Play(),getSelf());
            })
            .matchAny(o -> log.info("received unknown message"))
            .build();
  }
}
