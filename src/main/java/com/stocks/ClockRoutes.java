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

public class ClockRoutes extends AllDirectives {
    
    final private ActorRef clockActor;
    final private LoggingAdapter log;


    public ClockRoutes(ActorSystem system, ActorRef clockActor) {
        this.clockActor = clockActor;
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
                pathPrefix("time", () ->
                    route(
                        getTime()
                    )
                ),
                pathPrefix("reset-time", () ->
                    route(
                        resetTime()
                    )
                )
        ));
    }

    private Route getTime() {
        return pathEnd(() ->
            route(
                get(() -> {
                CompletionStage<ClockActor.Clock> clock = Patterns
                        .ask(clockActor, new ClockMessages.GetTime(), timeout)
                        .thenApply(ClockActor.Clock.class::cast);

                return onSuccess(() -> clock,
                    time -> {
                            return complete(StatusCodes.OK, time, Jackson.marshaller());
                    }
                    );
                })
            )
        );
    }

        private Route resetTime() {
        return pathEnd(() ->
            route(
                get(() -> {
                CompletionStage<ClockActor.Clock> clock = Patterns
                        .ask(clockActor, new ClockMessages.ResetTime(), timeout)
                        .thenApply(ClockActor.Clock.class::cast);

                return onSuccess(() -> clock,
                    time -> {
                            return complete(StatusCodes.OK, time, Jackson.marshaller());
                    }
                    );
                })
            )
        );
    }

}
