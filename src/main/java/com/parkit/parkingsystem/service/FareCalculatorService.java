package com.parkit.parkingsystem.service;

import java.time.Duration;
import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if((ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Duration ticketDuration = Duration.between(ticket.getInTime(), ticket.getOutTime());
        Duration paidDuration = ticketDuration.minus(Fare.FREETIME).isNegative() ? Duration.ZERO : ticketDuration.minus(Fare.FREETIME);

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(paidDuration.toMinutes() * Fare.CAR_RATE_PER_HOUR / 60);
                break;
            }
            case BIKE: {
                ticket.setPrice(paidDuration.toMinutes() * Fare.BIKE_RATE_PER_HOUR / 60);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}