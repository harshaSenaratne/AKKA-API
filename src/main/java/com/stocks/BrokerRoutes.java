package com.stocks;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.stocks.BankMessages.*;
import com.stocks.MarketActor.*;
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
import com.stocks.MarketActor.SaleTransaction;

/**
 * Routes can be defined in separated classes like shown in here
 */
public class BrokerRoutes extends AllDirectives {
    final private ActorRef brokerActor;
    final private ActorRef marketActor;
    final private ActorRef bankActor;
    final private LoggingAdapter log;


    public BrokerRoutes(ActorSystem system, ActorRef brokerActor, ActorRef marketActor,ActorRef bankActor) {
        this.brokerActor = brokerActor;
        this.marketActor = marketActor;
        this.bankActor = bankActor;
        log = Logging.getLogger(system, this);
    }

    // Required by the `ask` (?) method below
    Duration timeout = Duration.ofSeconds(5l); // usually we'd obtain the timeout from the system's configuration

    /**
     * This method creates one route (of possibly many more that will be part of your Web App)
     */

    public Route routes() {
        return route(concat(
            pathPrefix("sell", () ->
                route(
                    addSale()
                )
            ),
            pathPrefix("buy-sale", () ->
                route(
                    buySale()
                )
            ),
            pathPrefix("buy-company", () ->
                route(
                    buyCompany()
                )
            )
        ));
    }

    private Route addSale() {
        return pathEnd(() ->
            route(
                post(() -> 
                    entity(
                        Jackson.unmarshaller(MarketActor.Sale.class),
                        sale->{
                            log.info("Adding sale of user:"+sale.getUserId());
                            CompletionStage<MarketActor.Sale> addSale = Patterns
                                    .ask(marketActor, new MarketMessages.AddSale(sale), timeout)
                                    .thenApply(MarketActor.Sale.class::cast);

                            return onSuccess(() -> addSale,
                                addedSale -> {
                                    if(addedSale.getUserId()>0){
                                        return complete(StatusCodes.OK, addedSale, Jackson.marshaller());
                                    }else{
                                        return complete(StatusCodes.INTERNAL_SERVER_ERROR,"Error adding sale!");
                                    }
                                });
                        }
                    ))
            )
        );
    }

        private Route buySale() {
        return pathEnd(() ->
            route(
                post(() -> 
                    entity(
                        Jackson.unmarshaller(MarketActor.SaleTransaction.class),
                        sale->{
                            log.info("Adding sale of user:"+sale.getSellerId());

                            CompletionStage<MarketActor.SaleTransaction> addSale = Patterns
                                    .ask(bankActor, new BankMessages.DoTransaction(sale), timeout)
                                    .thenApply(MarketActor.SaleTransaction.class::cast);

                            return onSuccess(() -> addSale,
                                addedSale -> {
                                    if(addedSale.getSellerId()>0){
                                        
                                        marketActor.tell(new MarketMessages.BuySale(addedSale),brokerActor);

                                        return complete(StatusCodes.OK, addedSale, Jackson.marshaller());
                                    }else{
                                        return complete(StatusCodes.INTERNAL_SERVER_ERROR,"Error buying sale!");
                                    }
                                });
                        }
                    ))
            )
        );
    }

    private Route buyCompany() {
        return pathEnd(() ->
            route(
                post(() -> 
                    entity(
                        Jackson.unmarshaller(MarketActor.Sale.class),
                        sale->{
                            log.info("Buying shares from company:"+sale.getCompanyId());
                            CompletionStage<BankActor.Account> addSale = Patterns
                                    .ask(bankActor, new BankMessages.AddBalance(sale.getUserId(),sale.getValue()), timeout)
                                    .thenApply(BankActor.Account.class::cast);

                            return onSuccess(() -> addSale,
                                account -> {
                                    if(account.getUserId()>0){
                                        
                                        marketActor.tell(new MarketMessages.Buy(sale),brokerActor);

                                        return complete(StatusCodes.OK, account, Jackson.marshaller());
                                    }else{
                                        return complete(StatusCodes.INTERNAL_SERVER_ERROR,"Error buying!");
                                    }
                                });
                        }
                    ))
            )
        );
    }
}
