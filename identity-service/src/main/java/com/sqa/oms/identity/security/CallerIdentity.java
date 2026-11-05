package com.sqa.oms.identity.security;

import java.util.Set;

/**
 * End user identity forwarded by the gateway through the {@code Sqa-User-Id} header, decoded by
 * {@link SqaClientIdHeaderAuthenticationFilter} since identity-service never sees the caller's
 * real JWT on that path.
 *
 * @param userId      business key of the end user that triggered the request
 * @param authorities scopes the end user holds, as granted by the gateway
 */
public record CallerIdentity(String userId, Set<String> authorities) {
}
