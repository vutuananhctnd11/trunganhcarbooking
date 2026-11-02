package com.example.trunganhcar.event;

import com.example.trunganhcar.entity.Trip;
import com.example.trunganhcar.enums.TripStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TripStatusChangedEvent extends ApplicationEvent {
    private final Trip trip;
    private final TripStatus previousStatus;
    private final TripStatus currentStatus;
    private final String note;

    public TripStatusChangedEvent(Object source, Trip trip, TripStatus previousStatus, TripStatus currentStatus, String note) {
        super(source);
        this.trip = trip;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
        this.note = note;
    }
}

