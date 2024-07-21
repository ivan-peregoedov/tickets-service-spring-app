package com.pepeg.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pepeg.application.entity.Event;
import com.pepeg.application.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User user);

    List<Event> findByCity(String city);

    Optional<Event> findById(Long id);

    void deleteById(Long id);
}
