package com.sqa.oms.order.db.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.lang.reflect.Method;

/**
 * Aspect for intercepting each method with the annotation @Transactional.
 * It is used to set the transactionDefinitionHolder, and this field is
 * used to check if the transaction intercepted is read-only or not.
 */
@Aspect
@Component
public final class TransactionDefinitionInterceptor implements Ordered {

    private static final ThreadLocal<TransactionDefinition> TRANSACTION_DEFINITION_HOLDER
            = new NamedThreadLocal<>("Current aspect-driven transaction definition");

    private final TransactionAttributeSource transactionAttributeSource;

    private final int orderMinus = 10;

    // before Spring's TransactionInterceptor
    private int order = Ordered.LOWEST_PRECEDENCE - orderMinus;

    /**
     * Constructor with dependency injection.
     *
     * @param transactionAttributeSource resolver of the @Transactional attributes of a method
     */
    public TransactionDefinitionInterceptor(final TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    /**
     * Get the definition of the current transaction.
     *
     * @return the current transaction definition
     */
    public static TransactionDefinition currentTransactionDefinition() {
        return TRANSACTION_DEFINITION_HOLDER.get();
    }

    /**
     * Return true if there is a transaction running and it is read-write.
     * Otherwise, returns false.
     *
     * @return boolean
     */
    public static boolean isCurrentTransactionReadWrite() {
        return TRANSACTION_DEFINITION_HOLDER.get() != null
                && !TRANSACTION_DEFINITION_HOLDER.get().isReadOnly();
    }

    /**
     * Intercepts each method annotated with @Transactional and saves
     * the attributes of the transaction in the field transactionDefinitionHolder.
     * When the transaction has finished, the transactionDefinitionHolder of
     * it is cleared to prevent the possible messages using the
     * transactionDefinitionHolder of previous transactions.
     *
     * @param joinPoint     the intercepted method call
     * @param transactional the @Transactional annotation of the intercepted method
     * @return the result of the intercepted method call
     * @throws Throwable whatever the intercepted method throws
     */
    @Around("@annotation(transactional)")
    public Object rememberTransactionDefinition(final ProceedingJoinPoint joinPoint,
                                                final Transactional transactional) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        TransactionDefinition transactionDefinition = transactionAttributeSource.getTransactionAttribute(method,
                targetClass);
        boolean restore = false;
        if (TRANSACTION_DEFINITION_HOLDER.get() == null) {
            // Store state to allow other components to determine if TX is read-only
            TRANSACTION_DEFINITION_HOLDER.set(transactionDefinition);
            restore = true;
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (restore) {
                TRANSACTION_DEFINITION_HOLDER.remove();
            }
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }
}
