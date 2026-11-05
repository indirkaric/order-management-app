package com.sqa.oms.order.service.security;

import java.util.Set;

public record CallerIdentity(String userId, Set<String> authorities) {
}
