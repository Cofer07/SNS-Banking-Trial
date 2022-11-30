package com.example.snsbankingrestapi;

import com.example.snsbankingrestapi.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository  extends CrudRepository<Account, Integer> {
}
