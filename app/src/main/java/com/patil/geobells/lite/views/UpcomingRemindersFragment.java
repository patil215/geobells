package com.patil.geobells.lite.views;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.GeobellsDataManager;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class UpcomingRemindersFragment extends Fragment {

    GeobellsDataManager dataManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_upcoming, container, false);
        dataManager = new GeobellsDataManager(rootView.getContext());
        ArrayList<Reminder> reminders = dataManager.getSavedReminders();
        ArrayList<Card> cards = new ArrayList<Card>();
        for(int i = 0; i < reminders.size(); i++) {
            ReminderCard card = new ReminderCard(rootView.getContext(), R.layout.card_upcoming, reminders.get(i));
            cards.add(card);
        }
        CardArrayAdapter cardArrayAdapter = new CardArrayAdapter(rootView.getContext(), cards);
        CardListView listView = (CardListView) rootView.findViewById(R.id.cardList);
        listView.setAdapter(cardArrayAdapter);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(1);

    }
}
