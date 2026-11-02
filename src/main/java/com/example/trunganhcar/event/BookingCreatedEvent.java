package com.example.trunganhcar.event;

import com.example.trunganhcar.entity.Trip;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingCreatedEvent extends ApplicationEvent {
    private final Trip trip;

    public BookingCreatedEvent(Object source, Trip trip) {
        super(source);
        this.trip = trip;
    }
}

