package ris58h.springreactivefibonacci;


import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import java.util.function.Function;

public class SpringReactiveFibonacciApplication {

    private static final String HOST = "localhost";
    private static final int PORT = 8888;

    public static void main(String[] args) {
        Function<Long, Mono<Long>> fibonacci = fibonacci(webClient());
        RouterFunction<ServerResponse> route = routerFunction(fibonacci);

        HttpHandler handler = RouterFunctions.toHttpHandler(route);

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create(HOST, PORT);
        httpServer.startAndAwait(adapter, null);
    }

    static RouterFunction<ServerResponse> routerFunction(Function<Long, Mono<Long>> fibonacci) {
        return RouterFunctions.route(RequestPredicates.GET("/{n}"), serverRequest -> {
            long n = Long.parseLong(serverRequest.pathVariable("n"));
            if (n <= 2) {
                return ServerResponse.ok().body(Mono.just(1L), Long.class);
            } else {
                Mono<Long> n_1 = fibonacci.apply(n - 1);
                Mono<Long> n_2 = fibonacci.apply(n - 2);
                Mono<Long> sum = n_1.and(n_2, Long::sum);
                return ServerResponse.ok().body(sum, Long.class);
            }
        });
    }

    static WebClient webClient() {
        return WebClient.create("http://" + HOST + ":" + PORT);
    }

    static Function<Long, Mono<Long>> fibonacci(WebClient webClient) {
        return n -> webClient.get()
                .uri("/{n}", n)
                .retrieve()
                .bodyToMono(String.class)
                .map(Long::valueOf);
    }
}
