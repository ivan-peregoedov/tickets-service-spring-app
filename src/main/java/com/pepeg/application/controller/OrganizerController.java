package com.pepeg.application.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pepeg.application.entity.Event;
import com.pepeg.application.entity.User;
import com.pepeg.application.service.EventService;
import com.pepeg.application.service.TicketService;
import com.pepeg.application.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@CrossOrigin
@RequestMapping("/organizer")
@RequiredArgsConstructor
public class OrganizerController {
    private final EventService eventService;

    private final UserService userService;

    private final TicketService ticketService;

    @GetMapping("events/all")
    public List<Event> getOrganizerEvents(Principal principal) {
        User org = userService.findUserByEmail(principal.getName());
        return eventService.getAllOrganizerEvents(org);
    }

    @PostMapping("events/create")
    public Event create(@RequestBody Event event, Principal principal) throws Exception {
        return eventService.saveEvent(event, principal.getName());

    }

    @PostMapping("events/edit")
    public Event edit(@RequestBody Event event, @RequestParam Long id) {
        return eventService.editEvent(event, id);
    }

    @DeleteMapping("events/delete")
    public void deleteEvent(@RequestParam Long id) {
        eventService.deleteEvent(id);
    }

    @DeleteMapping("tickets/delete")
    public void deleteTicket(@RequestParam Long id) {
        ticketService.deleteTicket(id);
    }

}
