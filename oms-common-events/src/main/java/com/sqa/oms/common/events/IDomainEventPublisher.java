package com.sqa.oms.common.events;

public interface IDomainEventPublisher {

    void publish(OmsEvent event);
}
