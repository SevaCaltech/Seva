package edu.caltech.seva.activities.Main.Fragments.Home;

import android.content.Context;
import android.content.SharedPreferences;

import com.amazonaws.logging.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.caltech.seva.helpers.PrefManager;
import edu.caltech.seva.models.User;

import static org.junit.Assert.*;

public class HomePresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    HomeContract.View view;

    @Mock
    Log log;

    private HomePresenter presenter;
    private PrefManager prefManager;

    @Before
    public void setUp() throws Exception {
        final SharedPreferences prefs = Mockito.mock(SharedPreferences.class);
        final Context context = Mockito.mock(Context.class);
        Mockito.when(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(prefs);
        prefManager = new PrefManager(context);
        presenter = new HomePresenter(view, prefManager);
    }

    /**
     * Test loadNotifications()
     */
    @Test
    public void testLoadNotifications() {
        fail();
    }

    @Test
    public void testLoadNoNotifications() {
        fail();
    }

    /**
     * Test loadUserInfo()
     */
    @Test
    public void testLoadUserInfoFirstTimeLaunch() {
        prefManager.setFirstTimeLaunch(true);

        fail();
    }

    @Test
    public void testLoadUserInfoNotFirstTimeLaunch() {
        ArrayList<String> toilets = new ArrayList<>(Arrays.asList("t1", "t2"));
        Set<String> toiletSet = new HashSet<>(toilets);
        User user = new User("test", "test@gmail.com", "11111", toilets);
        Mockito.when(prefManager.isFirstTimeLaunch()).thenReturn(false);
        Mockito.when(prefManager.getEmail()).thenReturn(user.getEmail());
        Mockito.when(prefManager.isGuest()).thenReturn(false);
        Mockito.when(prefManager.getUid()).thenReturn(user.getUid());
        Mockito.when(prefManager.getUsername()).thenReturn(user.getName());
        Mockito.when(prefManager.getToilets()).thenReturn(toiletSet);

        presenter.loadUserInfo();

        Mockito.verify(view).showUserInfo(user);
    }

    @Test
    public void testLoadInvalidUser() {
        fail();
    }
}