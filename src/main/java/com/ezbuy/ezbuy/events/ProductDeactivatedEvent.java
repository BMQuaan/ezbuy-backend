package com.ezbuy.ezbuy.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductDeactivatedEvent extends ApplicationEvent {
    private final Integer productId;

    public ProductDeactivatedEvent(Object source, Integer productId) {
        super(source);
        this.productId = productId;
    }
}