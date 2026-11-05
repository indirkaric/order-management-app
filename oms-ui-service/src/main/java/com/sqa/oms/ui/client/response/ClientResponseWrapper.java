package com.sqa.oms.ui.client.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Raw status and body of a downstream JSON response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseWrapper {

    private HttpStatus statusCode;

    private Object responseBody;
}
