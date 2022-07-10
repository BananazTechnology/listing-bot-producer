package tech.bananaz.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tech.bananaz.bot.models.ListingEvent;

@Repository
public interface ListingEventRepository extends JpaRepository<ListingEvent, Long> {}
