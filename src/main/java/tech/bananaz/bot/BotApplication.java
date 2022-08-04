package tech.bananaz.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import antlr.debug.Event;
import tech.bananaz.bot.BotApplication;
import tech.bananaz.models.Listing;
import tech.bananaz.repositories.EventPagingRepository;
import tech.bananaz.repositories.ListingConfigPagingRepository;

@SpringBootApplication
@ComponentScan({"tech.bananaz.*"})
@EnableJpaRepositories(basePackageClasses = {
	EventPagingRepository.class, 
	ListingConfigPagingRepository.class})
@EntityScan(basePackageClasses = {
	Event.class, 
	Listing.class})
public class BotApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BotApplication.class, args);
	}
}
