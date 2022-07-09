package tech.bananaz.bot.models;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ContractCollection {

	private ArrayList<Contract> contracts;
	
	public ContractCollection() {
		this.contracts = new ArrayList<>();
	}
	
	public void addContract(Contract newContract) {
		this.contracts.add(newContract);
	}
	
	public void removeContract(Contract newContract) {
		this.contracts.remove(newContract);
	}
	
	public int size() {
		return contracts.size();
	}
	
	public String toString() {
		Contract[] simpArrContracts = new Contract[contracts.size()];
		return Arrays.deepToString(simpArrContracts);
	}
	
	public boolean isWatchingAddress(String otherAddress) {
		boolean response = false;
		for (Contract contract : contracts) {
			if(contract.getContractAddress().equalsIgnoreCase(otherAddress)) response = true;
		}
		return response;
	}

	public Contract getContractById(Long id) {
		Contract response = null;
		for (Contract contract : contracts) {
			if(contract.getId() == id) response = contract;
		}
		return response;
	}
}
