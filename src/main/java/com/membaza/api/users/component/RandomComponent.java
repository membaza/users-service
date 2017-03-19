package com.membaza.api.users.component;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface RandomComponent {

    long nextLong();

    String nextString(int length);

}