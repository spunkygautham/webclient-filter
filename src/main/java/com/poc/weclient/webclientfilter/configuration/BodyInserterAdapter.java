package com.poc.weclient.webclientfilter.configuration;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
class BodyInserterAdapter implements BodyInserter<Object, ClientHttpRequest> {

    private static final int DEFAULT_BUFFER_SIZE = 256 * 256;


    private final BodyInserter<?, ? super ClientHttpRequest> delegate;

    public BodyInserterAdapter(BodyInserter<?, ? super ClientHttpRequest> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<Void> insert(ClientHttpRequest outputMessage, Context context) {
        ClientHttpRequestDecorator requestDecorator = new ClientHttpRequestDecorator(outputMessage) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                return DataBufferUtils.join(body)
                        .flatMap(buffer -> handleBuffer(buffer));
            }

            private Mono<Void> handleBuffer(DataBuffer buffer) {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                DataBufferUtils.release(buffer);
                String data = new String(bytes, StandardCharsets.UTF_8);
                log.info("data= || " + data);
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
            }
        };
        return delegate.insert(requestDecorator, context);
    }

}