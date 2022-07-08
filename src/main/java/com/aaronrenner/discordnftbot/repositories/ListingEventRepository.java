package com.aaronrenner.discordnftbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aaronrenner.discordnftbot.models.ListingEvent;

@Repository
public interface ListingEventRepository extends JpaRepository<ListingEvent, Long> {}
