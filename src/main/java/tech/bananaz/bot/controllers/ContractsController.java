package tech.bananaz.bot.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tech.bananaz.bot.models.Contract;
import tech.bananaz.bot.models.ContractCollection;

@RestController
@RequestMapping(value = "/contracts", produces = "application/json")
public class ContractsController {
	
	@Autowired
	private ContractCollection contracts;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractsController.class);
	
	@GetMapping
	public List<Contract> readContracts() {
		LOGGER.debug("The GET endpoint was accessed");
		return this.contracts.getContracts();
	}

}
