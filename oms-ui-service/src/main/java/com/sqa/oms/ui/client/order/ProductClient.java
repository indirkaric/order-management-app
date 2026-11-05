package com.sqa.oms.ui.client.order;

import com.sqa.oms.ui.client.BaseClient;
import com.sqa.oms.ui.client.response.ClientFileResponseWrapper;
import com.sqa.oms.ui.client.response.ClientResponseWrapper;
import com.sqa.oms.ui.constants.Constants;
import com.sqa.oms.ui.modules.order.model.ProductListRequest;
import com.sqa.oms.ui.service.security.IScopeContext;
import com.sqa.oms.ui.util.QueryParamsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Client for the product resources of the order service, including document upload and download.
 */
@Service
public class ProductClient extends BaseClient {

    private static final String PRODUCTS_PATH = "products";

    private static final String DOCUMENTS_PATH = "/documents";

    @Value("${client.order.base-uri}")
    private String baseUri;

    /**
     * Constructor with dependency injection.
     *
     * @param webClient    shared WebClient
     * @param scopeContext resolver of the authenticated caller
     */
    public ProductClient(final WebClient webClient, final IScopeContext scopeContext) {
        super(webClient, scopeContext);
    }

    /**
     * Retrieves the product list.
     *
     * @param request  the query parameters
     * @param pageable paging information
     * @return downstream response
     */
    public ClientResponseWrapper getProducts(final ProductListRequest request, final Pageable pageable) {
        MultiValueMap<String, String> params = QueryParamsBuilder.create();
        QueryParamsBuilder.addIfPresent(params, "sku", request.getSku());
        QueryParamsBuilder.addIfPresent(params, "textSearch", request.getTextSearch());
        QueryParamsBuilder.addPaging(params, pageable);

        return get(PRODUCTS_PATH, params);
    }

    /**
     * Retrieves a single product.
     *
     * @param productId product identifier
     * @return downstream response
     */
    public ClientResponseWrapper getProduct(final String productId) {
        return get(PRODUCTS_PATH + "/" + productId, null);
    }

    /**
     * Creates a product.
     *
     * @param body request body
     * @return downstream response
     */
    public ClientResponseWrapper createProduct(final Object body) {
        return send(HttpMethod.POST, PRODUCTS_PATH, body);
    }

    /**
     * Retrieves the documents of a product. order-service does not support filtering this list,
     * so no query parameters are forwarded.
     *
     * @param productId product identifier
     * @return downstream response
     */
    public ClientResponseWrapper getProductDocuments(final String productId) {
        return get(PRODUCTS_PATH + "/" + productId + DOCUMENTS_PATH, null);
    }

    /**
     * Forwards a product document to the order service.
     *
     * @param productId product identifier
     * @param file      inbound file
     * @return downstream response
     */
    public ClientResponseWrapper uploadProductDocument(final String productId, final MultipartFile file) {
        return sendMultipart(PRODUCTS_PATH + "/" + productId + DOCUMENTS_PATH, file);
    }

    /**
     * Downloads a product document.
     *
     * @param productId  product identifier
     * @param documentId document identifier
     * @return downstream binary response
     */
    public ClientFileResponseWrapper downloadProductDocument(final String productId, final String documentId) {
        return download(PRODUCTS_PATH + "/" + productId + DOCUMENTS_PATH + "/" + documentId + "/download");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String baseUri() {
        return baseUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String clientId() {
        return Constants.GATEWAY_ORDER_CLIENT;
    }
}
