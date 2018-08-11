package dev.bcv.reservationservice;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@SpringBootApplication
public class ReservationServiceApplication {

    @Bean
    ReactiveUserDetailsService authentication() {
        return new MapReactiveUserDetailsService(User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin")
                .roles("USER")
                .build());
    }

    @Bean
    SecurityWebFilterChain config(ServerHttpSecurity security) {
        return security
                .csrf().disable()
                .httpBasic()
                .and()
                .authorizeExchange().pathMatchers("/reservations").authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }

    @Bean
    RouterFunction<?> routes(ReservationRepository reservationRepository) {
        return RouterFunctions.route(GET("/reservations"),
                serverRequest -> ServerResponse.ok().body(reservationRepository.findAll(), Reservation.class));
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

}

interface ReservationRepository extends ReactiveMongoRepository<Reservation, String> {
}

@Component
class Initializer implements ApplicationRunner {

    private final ReservationRepository reservationRepository;

    public Initializer(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        this.reservationRepository.deleteAll()
                .thenMany(
                        Flux.just("Josh", "Martin", "Anna", "Andreas", "Jana", "Alok", "Anki", "Anders")
                                .map(name -> new Reservation(null, name))
                                .flatMap(this.reservationRepository::save))
                .thenMany(this.reservationRepository.findAll())
                .subscribe(System.out::println);
    }
}

//@RestController
//class ReseervationRestController {
//    private final ReservationRepository reservationRepository;
//    ReseervationRestController(final ReservationRepository reservationRepository) {
//        this.reservationRepository = reservationRepository;
//    }
//    @GetMapping("/reservations")
//    Publisher<Reservation> getReservations(){
//        return this.reservationRepository.findAll();
//    }
//}

@Document
@AllArgsConstructor
@NoArgsConstructor
@Data
class Reservation {
    @Id
    private String id;
    private String reservationName;
}
