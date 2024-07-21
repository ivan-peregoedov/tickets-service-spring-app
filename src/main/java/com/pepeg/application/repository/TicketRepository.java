package com.pepeg.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pepeg.application.entity.Ticket;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    void deleteById(Long id);

}