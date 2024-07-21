package com.pepeg.application.service;


import org.springframework.stereotype.Service;

import com.pepeg.application.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

}
