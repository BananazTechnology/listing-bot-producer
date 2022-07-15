package tech.bananaz.bot.controllers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

	@Value("${info.version:unknown}")
	private String appVersion;
	@Value("${info.name:unknown}")
	private String appName;
	private static final String SERVICE_HEADER = "X-SERVICE";
	private static final String SERVICE_VALUE_FORMAT = "%s/%s";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ContractsController.class);
	
	@GetMapping
	public ResponseEntity<List<Contract>> readContracts() {
		LOGGER.debug("The GET endpoint was accessed");
		return ResponseEntity
					.ok()
					.header(SERVICE_HEADER, String.format(SERVICE_VALUE_FORMAT, appName, appVersion))
					.body(this.contracts.getContracts());
	}

}