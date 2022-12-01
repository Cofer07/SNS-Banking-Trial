package com.example.snsbankingrestapi;

import com.example.snsbankingrestapi.Account;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository  extends CrudRepository<Account, Integer> {
	
	@Query(value = "select a from Account a where a.user.userid = ?1")
	List<Account> findByUserId(int userId);
	
}
