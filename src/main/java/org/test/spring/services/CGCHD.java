package org.test.spring.services;

import com.sun.jna.Library;

public interface CGCHD extends Library {
    public int main(int arc, String[] argv);
}
