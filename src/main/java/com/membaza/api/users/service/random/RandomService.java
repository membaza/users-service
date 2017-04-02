package com.membaza.api.users.service.random;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface RandomService {

    long nextLong();

    String nextString(int length);

}