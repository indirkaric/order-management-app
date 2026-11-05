package com.sqa.oms.ui.config;

import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.Check;
import com.sqa.oms.ui.util.StringHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Builds the single WebClient shared by every downstream client.
 */
@Configuration
public class WebClientConfig {

    private final int bufferSize = 4608;

    private final IScopeContext scopeContext;

    /**
     * Constructor with dependency injection.
     *
     * @param scopeContext access to the authenticated caller
     */
    public WebClientConfig(final IScopeContext scopeContext) {
        Check.isNotNull(scopeContext, "scopeContext");
        this.scopeContext = scopeContext;
    }

    /**
     * Shared WebClient with the caller identity filter registered as a default filter.
     *
     * @return WebClient
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(callerIdentityFilter())
                .exchangeStrategies(buildExchangeStrategy())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
                .build();
    }

    /**
     * Attaches the base64 encoded caller identity to every outbound call.
     *
     * @return ExchangeFilterFunction
     */
    public ExchangeFilterFunction callerIdentityFilter() {
        return (request, next) -> {
            // Resolved at subscription time, which happens on the servlet thread because every
            // downstream call is blocking; the SecurityContext ThreadLocal is still visible here.
            String callerIdentity = scopeContext.getBase64EncodedUserInfo();

            if (StringHelper.isEmpty(callerIdentity)) {
                return next.exchange(request);
            }

            ClientRequest newRequest = ClientRequest.from(request)
                    .header(Constants.SQA_USER_ID_HEADER, callerIdentity)
                    .build();

            return next.exchange(newRequest);
        };
    }

    private ExchangeStrategies buildExchangeStrategy() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(bufferSize * bufferSize))
                .build();
    }
}
