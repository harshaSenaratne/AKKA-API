package com.stocks;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.stocks.BankMessages.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;

/**
 * Routes can be defined in separated classes like shown in here
 */
public class BankRoutes extends AllDirectives {
    final private ActorRef bankActor;
    final private LoggingAdapter log;


    public BankRoutes(ActorSystem system, ActorRef bankActor) {
        this.bankActor = bankActor;
        log = Logging.getLogger(system, this);
    }

    // Required by the `ask` (?) method below
    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */

    public Route routes() {
        return route(pathPrefix("get-balance", () ->
            route(
                getBalance()
            )
        ));
    }

    private Route getBalance() {
        return pathEnd(() ->
            route(
                get(() -> {
                    
                CompletionStage<BankActor.Bank> bankStatus = Patterns
                        .ask(bankActor, new BankMessages.GetBalance(), timeout)
                        .thenApply(BankActor.Bank.class::cast);

                return onSuccess(() -> bankStatus,
                    bank -> {
                            return complete(StatusCodes.OK, bank, Jackson.marshaller());
                    }
                    );

                })
            )
        );
    }
}
