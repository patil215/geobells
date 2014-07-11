package com.patil.geobells.lite.views;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.GeobellsDataManager;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class CompletedRemindersFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_completed, container, false);
        final GeobellsDataManager dataManager = new GeobellsDataManager(rootView.getContext());
        final ArrayList<Reminder> reminders = dataManager.getSavedReminders();
        ArrayList<Card> cards = new ArrayList<Card>();
        for(int i = 0; i < reminders.size(); i++) {
            final int index = i;
            if(reminders.get(i).completed) {
                ReminderCard card = new ReminderCard(rootView.getContext(), R.layout.card_reminder, reminders.get(i), i);
                card.setSwipeable(true);
                card.setOnSwipeListener(new Card.OnSwipeListener() {
                    @Override
                    public void onSwipe(Card card) {
                        Toast.makeText(rootView.getContext(), rootView.getContext().getString(R.string.toast_reminder_swipe_uncompleted), Toast.LENGTH_SHORT).show();
                        Reminder reminder = reminders.get(index);
                        reminder.completed = false;
                        dataManager.saveReminders(reminders);
                    }
                });
                cards.add(card);
            }
        }
        CardArrayAdapter cardArrayAdapter = new CardArrayAdapter(rootView.getContext(), cards);
        CardListView listView = (CardListView) rootView.findViewById(R.id.cardList);
        listView.setAdapter(cardArrayAdapter);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(2);

    }
}
