package edu.caltech.seva.activities.Main;

import java.util.Set;

public interface MainViewI {

    void displayToilets(Set<String> toilets);

    void displayNoToilets();

    void displayError();
}
