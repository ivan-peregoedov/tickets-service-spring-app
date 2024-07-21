package com.pepeg.application.service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pepeg.application.entity.Event;
import com.pepeg.application.entity.Ticket;
import com.pepeg.application.entity.User;
import com.pepeg.application.repository.EventRepository;
import com.pepeg.application.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getAllOrganizerEvents(User organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    public List<Event> getEventsByCity(String city) {
        return eventRepository.findByCity(city);
    }

    public Event saveEvent(Event event, String userEmail) {
        User user = userRepository.findByEmail(userEmail).get();
        event.setOrganizer(user);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(event.getDateTimeString(), formatter);
        event.setStartDate(localDateTime);
        for (Ticket ticket : event.getTickets()) {
            ticket.setEvent(event);
        }

        return eventRepository.save(event);
    }

    public Event editEvent(Event updatedEvent, Long eventId) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Мероприятие не найдено с id: " + eventId));

        existingEvent.setName(updatedEvent.getName());
        existingEvent.setCity(updatedEvent.getCity());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setGenre(updatedEvent.getGenre());
        existingEvent.setDateTimeString(updatedEvent.getDateTimeString());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(updatedEvent.getDateTimeString(), formatter);
        existingEvent.setStartDate(localDateTime);

        existingEvent.setParent_genre(updatedEvent.getParent_genre());
        existingEvent.setTickets(updatedEvent.getTickets());

        for (Ticket ticket : existingEvent.getTickets()) {
            ticket.setEvent(existingEvent);
        }

        return eventRepository.save(existingEvent);

    }

    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }
}
