package com.example.snsbankingrestapi;

import com.example.snsbankingrestapi.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository  extends CrudRepository<Transaction, Integer> {
}
