package dev.bcv.reservationclient;

import org.reactivestreams.Publisher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ReservationClientApplication {

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .filter(ExchangeFilterFunctions.basicAuthentication("admin", "admin"))
                .build();
    }

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder rlb) {
       return rlb
                .routes()
                .route(predicateSpec -> predicateSpec.path("/guides")
                         .uri("http://spring.io:80/guides"))
               .build();
    }

    @Bean
    RouterFunction<?> routerFunction(WebClient webClient) {
        return RouterFunctions.route(RequestPredicates.GET("/names"), serverRequest -> {
            Publisher<String> namesPublisher = webClient
                    .get()
                    .uri("http://localhost:8080/reservations")
                    .retrieve()
                    .bodyToFlux(Reservation.class)
                    .map(Reservation::getReservationName)
                    .map(name -> name + " ");
            return ServerResponse.ok().body(namesPublisher, String.class);
        });


    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class Reservation {
    private String id;
    private String reservationName;
}
