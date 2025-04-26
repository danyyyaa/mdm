package com.danya.mdm.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionalService {

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 3000)
    public void runInNewTransaction(Runnable runnable) {
        runnable.run();
    }
}

