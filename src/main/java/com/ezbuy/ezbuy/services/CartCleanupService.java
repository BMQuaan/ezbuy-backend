package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.entities.Cart;
import com.ezbuy.ezbuy.entities.User;
import java.util.List;

public interface CartCleanupService {
    List<Cart> cleanCartAndGetValidItems(User user);
}