package com.stocks;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.stocks.MarketActor.Market;
import com.stocks.MarketActor.Company;
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

public class AnalystRoutes extends AllDirectives {
    
    final private ActorRef marketActor;
    final private LoggingAdapter log;


    public AnalystRoutes(ActorSystem system, ActorRef marketActor) {
        this.marketActor = marketActor;
        log = Logging.getLogger(system, this);
    }

    // Required by the `ask` (?) method below
    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */
    //#all-routes
    public Route routes() {
        return route(
            concat(
                pathPrefix("tip", () ->
                    route(
                        getTip()
                    )
                )
        ));
    }

    private Route getTip() {
        return pathEnd(() ->
            route(
                get(() -> {

                CompletionStage<MarketActor.Market> getTip = Patterns
                        .ask(marketActor, new MarketMessages.GetCompanies(), timeout)
                        .thenApply(MarketActor.Market.class::cast);

               try {
                    Market market = getTip.toCompletableFuture().get();

                    Company company = market.getCompanies().get(((int)System.currentTimeMillis())%10);

                    return complete(StatusCodes.OK, company, Jackson.marshaller());
                }
                catch(Exception e) {
                    return complete(StatusCodes.OK, "Error!");
                }
                // CompletionStage<MarketActor.Company> getTip = Patterns
                //         .ask(marketActor, new AnalystMessages.GetTip(), timeout)
                //         .thenApply(MarketActor.Company.class::cast);

                // return onSuccess(() -> getTip,
                //     company -> {
                //             return complete(StatusCodes.OK, company, Jackson.marshaller());
                //     }
                //     );
                })
            )
        );
    }


}
