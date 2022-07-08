package com.aaronrenner.discordnftbot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aaronrenner.discordnftbot.models.ListingConfig;

@Repository
public interface ListingConfigRepository extends JpaRepository<ListingConfig, Long> {}
