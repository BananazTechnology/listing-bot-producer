package com.aaronrenner.discordnftbot.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aaronrenner.discordnftbot.models.Contract;
import com.aaronrenner.discordnftbot.models.ContractCollection;

@RestController
public class ContractsController {
	
	@Autowired
	private ContractCollection contracts;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractsController.class);
	private final String CONTENT_TYPE  = "application/json";
	private final String CONTRACT_PATH = "/contracts";
	
	@GetMapping(path = CONTRACT_PATH, produces = CONTENT_TYPE)
	public List<Contract> readContracts() {
		LOGGER.debug("The {} endpoint was accessed", CONTRACT_PATH);
		return this.contracts.getContractCollection();
	}

}
