package edu.caltech.seva.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void testEquals() {
        User user = new User("test", "test@gmail.com", "11111",
                new ArrayList<>(Arrays.asList("t1", "t2")));
        User user1 = new User("test", "test@gmail.com", "11111",
                new ArrayList<>(Arrays.asList("t1", "t2")));
        assertEquals(user,user1);
    }
}