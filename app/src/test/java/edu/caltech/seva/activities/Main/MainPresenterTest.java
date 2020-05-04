package edu.caltech.seva.activities.Main;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.caltech.seva.repositories.UserRepositoryI;

import static org.junit.Assert.*;

public class MainPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    UserRepositoryI userRepository;

    @Mock
    MainViewI view;

    private MainPresenter presenter;

    @Before
    public void setUp() {
        presenter = new MainPresenter(view, userRepository);
    }

    @Test
    public void shouldPassToiletsToView() {
        Set<String> toiletList = new HashSet<>(Arrays.asList("t1", "t2", "t3"));
        Mockito.when(userRepository.getToilets()).thenReturn(toiletList);

        presenter.loadToilets();

        Mockito.verify(view).displayToilets(toiletList);
    }

    @Test
    public void shouldHandleNoToilets() {
        Mockito.when(userRepository.getToilets()).thenReturn(Collections.<String>emptySet());

        presenter.loadToilets();

        Mockito.verify(view).displayNoToilets();
    }

    @Test
    public void shouldHandleError() {
        Mockito.when(userRepository.getToilets()).thenThrow(new RuntimeException("boom"));

        presenter.loadToilets();

        Mockito.verify(view).displayError();
    }
}