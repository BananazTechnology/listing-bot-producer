package com.aaronrenner.discordnftbot.models;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ContractCollection {

	private ArrayList<Contract> contractCollection;
	
	public ContractCollection() {
		this.contractCollection = new ArrayList<>();
	}
	
	public void addContract(Contract newContract) {
		this.contractCollection.add(newContract);
	}
	
	public void removeContract(Contract newContract) {
		this.contractCollection.remove(newContract);
	}
	
	public int size() {
		return contractCollection.size();
	}
	
	public String toString() {
		Contract[] contracts = new Contract[contractCollection.size()];
		return Arrays.deepToString(contracts);
	}
	
	public boolean isWatchingAddress(String otherAddress) {
		boolean response = false;
		for (Contract contract : contractCollection) {
			if(contract.getContractAddress().equalsIgnoreCase(otherAddress)) response = true;
		}
		return response;
	}

	public Contract getContractById(Long id) {
		Contract response = null;
		for (Contract contract : contractCollection) {
			if(contract.getId() == id) response = contract;
		}
		return response;
	}
}
