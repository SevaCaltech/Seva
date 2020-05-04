package edu.caltech.seva.activities.Main;

import java.util.Set;

import edu.caltech.seva.repositories.UserRepositoryI;

public class MainPresenter {
    private MainViewI view;
    private UserRepositoryI userRepository;

    public MainPresenter(MainViewI view, UserRepositoryI userRepositoy) {
        this.view = view;
        this.userRepository = userRepositoy;
    }


    public void loadToilets() {
        try {
            Set<String> toilets = userRepository.getToilets();
            if (toilets.isEmpty()) {
                view.displayNoToilets();
            } else {
                view.displayToilets(toilets);
            }
        } catch (Exception e) {
            view.displayError();
        }
    }
}
