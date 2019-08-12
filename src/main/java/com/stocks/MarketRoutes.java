package com.stocks;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.stocks.MarketActor.Market;
import com.stocks.MarketMessages.ActionPerformed;
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
//#user-routes-class
public class MarketRoutes extends AllDirectives {
    //#user-routes-class
    final private ActorRef marketActor;
    final private LoggingAdapter log;


    public MarketRoutes(ActorSystem system, ActorRef marketActor) {
        this.marketActor = marketActor;
        log = Logging.getLogger(system, this);
    }

    // Required by the `ask` (?) method below
    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    public Route routes() {
        return route(
            concat(
                pathPrefix("market-status", () ->
                    route(
                        getStatus()
                    )
                ),
                pathPrefix("change-values", () ->
                    route(
                        changeValues()
                    )
                )
        ));
    }

    private Route getStatus() {
        return pathEnd(() ->
            route(
                get(() -> {
                    
                CompletionStage<MarketActor.Market> marketStatus = Patterns
                        .ask(marketActor, new MarketMessages.GetCompanies(), timeout)
                        .thenApply(MarketActor.Market.class::cast);

                return onSuccess(() -> marketStatus,
                    market -> {
                            return complete(StatusCodes.OK, market, Jackson.marshaller());
                    }
                    );

                })
            )
        );
    }

    private Route changeValues() {
        return pathEnd(() ->
            route(
                get(() -> {
                    
                CompletionStage<MarketActor.Market> changeValues = Patterns
                        .ask(marketActor, new MarketMessages.ChangeCompanyValues(), timeout)
                        .thenApply(MarketActor.Market.class::cast);

                return onSuccess(() -> changeValues,
                    market -> {
                            return complete(StatusCodes.OK, market, Jackson.marshaller());
                    }
                    );

                })
            )
        );
    }
}
