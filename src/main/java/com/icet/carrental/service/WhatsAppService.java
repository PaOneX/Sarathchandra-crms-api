package com.icet.carrental.service;

import com.icet.carrental.model.Booking;
import com.icet.carrental.model.Car;
import com.icet.carrental.model.User;

public interface WhatsAppService {

    boolean sendBookingConfirmation(User user, Booking booking, Car car);
}
