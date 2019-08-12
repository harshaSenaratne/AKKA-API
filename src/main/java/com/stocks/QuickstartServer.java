package com.stocks;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import static akka.http.javadsl.server.Directives.*;

//#main-class
public class QuickstartServer extends AllDirectives {

    // set up ActorSystem and other dependencies here
    private final UserRoutes userRoutes;
    private final MarketRoutes marketRoutes;
    private final ClockRoutes clockRoutes;
    private final BankRoutes bankRoutes;
    private final BrokerRoutes brokerRoutes;
    private final AnalystRoutes analystRoutes;

    public QuickstartServer(ActorSystem system, ActorRef userRegistryActor,ActorRef marketActor,ActorRef clockActor,ActorRef bankActor,ActorRef brokerActor,ActorRef aiActor) {
        userRoutes = new UserRoutes(system, userRegistryActor,bankActor,clockActor,aiActor,marketActor);
        marketRoutes = new MarketRoutes(system,marketActor);
        clockRoutes = new ClockRoutes(system, clockActor);
        bankRoutes = new BankRoutes(system,bankActor);
        brokerRoutes = new BrokerRoutes(system,brokerActor,marketActor,bankActor);
        analystRoutes = new AnalystRoutes(system,marketActor);
    }
    //#main-class

    public static void main(String[] args) throws Exception {
        //#server-bootstrapping
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create("stockAkkaHttpServer");

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        //#server-bootstrapping

        ActorRef userRegistryActor = system.actorOf(UserRegistryActor.props(), "userRegistryActor");
        ActorRef marketActor = system.actorOf(MarketActor.props(), "marketActor");
        ActorRef clockActor = system.actorOf(ClockActor.props(), "clockActor");
        ActorRef bankActor = system.actorOf(BankActor.props(), "bankActor");
        ActorRef brokerActor = system.actorOf(BrokerActor.props(), "brokerActor");
        ActorRef aiActor = system.actorOf(PlayerAIActor.props(), "aiActor");
        ActorRef analystActor = system.actorOf(AnalystActor.props(), "analystActor");

        aiActor.tell(new PlayerAIMessages.SetActors(clockActor,marketActor,userRegistryActor,marketActor),aiActor);
        marketActor.tell(new MarketMessages.SetActors(bankActor),bankActor);

        //#http-server
        //In order to access all directives we need an instance where the routes are define.
        QuickstartServer app = new QuickstartServer(system, userRegistryActor,marketActor,clockActor,bankActor,brokerActor,aiActor);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);

        System.out.println("Server online at http://localhost:8080/");
        //#http-server
    }

    //#main-class
    /**
     * Here you can define all the different routes you want to have served by this web server
     * Note that routes might be defined in separated classes like the current case
     */
    protected Route createRoute() {
        return concat(userRoutes.routes(),marketRoutes.routes(),clockRoutes.routes(),bankRoutes.routes(),brokerRoutes.routes(),analystRoutes.routes());
    }
}
//#main-class


