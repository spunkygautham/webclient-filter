package com.poc.weclient.webclientfilter.configuration.filter;

import com.google.gson.Gson;
import com.poc.weclient.webclientfilter.model.Transaction;
import com.poc.weclient.webclientfilter.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RequestInterceptor implements ExchangeFilterFunction {

    private static final int DEFAULT_BUFFER_SIZE = 256 * 256;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        BodyInserter<?, ? super ClientHttpRequest> inserter = request.body();
        ClientRequest newReq = ClientRequest.from(request)
                .body((outputMessage, context) -> {
                    ClientHttpRequestDecorator requestDecorator = new ClientHttpRequestDecorator(outputMessage) {
                        @Override
                        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                            return DataBufferUtils.join(body).flatMap(buffer -> {
                                byte[] bytes = new byte[buffer.readableByteCount()];
                                buffer.read(bytes);
                                DataBufferUtils.release(buffer);
                                String data = new String(bytes, StandardCharsets.UTF_8);
                                log.info("data= || " + data);
                                /* performing an operation */

                                var transaction = new Gson().fromJson(data, Transaction.class);
                                if (transaction != null) {
                                    transaction.setItemAmount(transaction.getItemAmount() * 2 / 5);
                                }
                                String encrypted = AesUtil.encrypt(new Gson().toJson(transaction));
                                log.info("encrypted || " + encrypted);
                                Resource res = new ByteArrayResource(encrypted.getBytes());
                                getHeaders().setContentLength(encrypted.getBytes().length);
                                Flux<DataBuffer> nb = DataBufferUtils.read(res, new DefaultDataBufferFactory(), DEFAULT_BUFFER_SIZE);
                                return super.writeWith(nb);
                            });
                        }
                    };
                    return inserter.insert(requestDecorator, context);
                }).build();

        return next.exchange(newReq);

    }
}
