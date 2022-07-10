package tech.bananaz.bot.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tech.bananaz.bot.models.ListingConfig;

@Repository
public interface ListingConfigRepository extends JpaRepository<ListingConfig, Long> {}
