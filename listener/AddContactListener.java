package com.lingtuan.firefly.listener;

/**
 * Add buddy listener
 */

public interface AddContactListener {
    void addContactCallback(String telephone, String relation, int addType, String friendNick);
}
