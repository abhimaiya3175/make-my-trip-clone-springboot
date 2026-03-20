package com.makemytrip.modules.hotels.service;

import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.repository.HotelRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;

    @PostConstruct
    public void seedHotelsIfMissing() {
        if (hotelRepository.count() >= 30) {
            return;
        }

        List<Hotel> hotels = List.of(
                createHotel("The Imperial Retreat", "Delhi", 7800, 25, "WiFi, Breakfast, Spa, Airport Shuttle"),
                createHotel("Connaught Grand", "Delhi", 6400, 22, "WiFi, Gym, Restaurant"),
                createHotel("Marine Bay Suites", "Mumbai", 8200, 20, "Sea View, WiFi, Pool, Breakfast"),
                createHotel("Andheri Business Inn", "Mumbai", 5100, 18, "WiFi, Work Desk, Breakfast"),
                createHotel("Silicon Residency", "Bangalore", 5600, 30, "WiFi, Co-working Lounge, Gym"),
                createHotel("Garden City Premium", "Bangalore", 6900, 16, "WiFi, Pool, Rooftop Dining"),
                createHotel("Park Street Palace", "Kolkata", 5900, 21, "WiFi, Breakfast, Heritage Tour"),
                createHotel("Howrah Comfort Stay", "Kolkata", 4200, 28, "WiFi, Family Rooms"),
                createHotel("Chennai Marina Inn", "Chennai", 5300, 24, "WiFi, Breakfast, Beach Access"),
                createHotel("T Nagar Suites", "Chennai", 4700, 19, "WiFi, Laundry, Business Center"),
                createHotel("Charminar Elite", "Hyderabad", 5000, 27, "WiFi, Local Cuisine, Parking"),
                createHotel("Hitech City Stay", "Hyderabad", 6200, 23, "WiFi, Gym, Airport Shuttle"),
                createHotel("Goa Beachfront Resort", "Goa", 9100, 14, "Beach Access, Pool, Water Sports"),
                createHotel("Panaji Central Hotel", "Goa", 4800, 26, "WiFi, Breakfast, City Tours"),
                createHotel("Pune Tech Park Hotel", "Pune", 5400, 22, "WiFi, Meeting Rooms, Gym"),
                createHotel("Ahmedabad Heritage House", "Ahmedabad", 4600, 20, "WiFi, Breakfast, Cultural Walk"),
                createHotel("Jaipur Royal Courtyard", "Jaipur", 6100, 18, "WiFi, Pool, Heritage Dining"),
                createHotel("Udaipur Lakeview Retreat", "Udaipur", 8600, 12, "Lake View, Spa, Fine Dining"),
                createHotel("Lucknow Nawabi Stay", "Lucknow", 4300, 24, "WiFi, Breakfast, Parking"),
                createHotel("Hazratganj Residency", "Lucknow", 5100, 19, "WiFi, Restaurant, Conference Hall"),
                createHotel("Jaipur Pink City Inn", "Jaipur", 4500, 26, "WiFi, Rooftop Cafe, City Tour Desk"),
                createHotel("Amber Fort View Hotel", "Jaipur", 6700, 17, "WiFi, Pool, Heritage Decor"),
                createHotel("Bhopal Lakeside Retreat", "Bhopal", 4800, 21, "WiFi, Lake View, Breakfast"),
                createHotel("Indore Business Suites", "Indore", 5200, 23, "WiFi, Work Desk, Airport Transfer"),
                createHotel("Surat Riverside Inn", "Surat", 4400, 25, "WiFi, Family Rooms, Parking"),
                createHotel("Vadodara Palace Hotel", "Vadodara", 5000, 20, "WiFi, Banquet Hall, Gym"),
                createHotel("Nagpur Orange City Stay", "Nagpur", 4700, 24, "WiFi, Breakfast, Laundry"),
                createHotel("Mysore Heritage Manor", "Mysore", 6200, 16, "WiFi, Garden, Cultural Evenings"),
                createHotel("Coimbatore Comfort Hub", "Coimbatore", 4600, 22, "WiFi, Business Center"),
                createHotel("Trivandrum Seabreeze", "Trivandrum", 5800, 18, "WiFi, Breakfast, Beach Access"),
                createHotel("Kochi Harbor Hotel", "Kochi", 6300, 19, "WiFi, Harbor View, Seafood Restaurant"),
                createHotel("Amritsar Golden Stay", "Amritsar", 5500, 20, "WiFi, Temple Shuttle, Breakfast")
        );

        hotelRepository.saveAll(hotels);
    }

    private Hotel createHotel(String name,
                              String location,
                              double pricePerNight,
                              int availableRooms,
                              String amenities) {
        Hotel hotel = new Hotel();
        hotel.sethotelName(name);
        hotel.setLocation(location);
        hotel.setPricePerNight(pricePerNight);
        hotel.setAvailableRooms(availableRooms);
        hotel.setamenities(amenities);
        return hotel;
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> getHotelById(String id) {
        return hotelRepository.findById(id);
    }

    public Hotel addHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    public Hotel editHotel(String id, Hotel updatedHotel) {
        Optional<Hotel> hotelOptional = hotelRepository.findById(id);
        if (hotelOptional.isPresent()) {
            Hotel hotel = hotelOptional.get();
            hotel.sethotelName(updatedHotel.gethotelName());
            hotel.setLocation(updatedHotel.getLocation());
            hotel.setAvailableRooms(updatedHotel.getAvailableRooms());
            hotel.setPricePerNight(updatedHotel.getPricePerNight());
            hotel.setamenities(updatedHotel.getamenities());
            return hotelRepository.save(hotel);
        }
        return null;
    }
}
