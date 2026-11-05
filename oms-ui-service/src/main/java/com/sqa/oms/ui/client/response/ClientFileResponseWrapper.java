package com.sqa.oms.ui.client.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Raw status, bytes and file headers of a downstream binary response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientFileResponseWrapper {

    private HttpStatus statusCode;

    private byte[] responseBody;

    private String contentType;

    private String contentDisposition;
}
