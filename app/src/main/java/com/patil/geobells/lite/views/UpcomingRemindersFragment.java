package com.patil.geobells.lite.views;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.utils.GeobellsDataManager;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class UpcomingRemindersFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_upcoming, container, false);
        final GeobellsDataManager dataManager = new GeobellsDataManager(rootView.getContext());
        final ArrayList<Reminder> reminders = dataManager.getUpcomingReminders();
        CardListView listView = (CardListView) rootView.findViewById(R.id.cardList);
        RelativeLayout noReminderLayout = (RelativeLayout) rootView.findViewById(R.id.layout_noreminders);
        if (reminders.size() > 0) {
            listView.setVisibility(View.VISIBLE);
            noReminderLayout.setVisibility(View.GONE);
            ArrayList<Card> cards = new ArrayList<Card>();
            for (int i = 0; i < reminders.size(); i++) {
                final int index = i;
                    ReminderCard card = new ReminderCard(rootView.getContext(), R.layout.card_reminder, reminders.get(i), i);
                    card.setSwipeable(true);
                    card.setOnSwipeListener(new Card.OnSwipeListener() {
                        @Override
                        public void onSwipe(Card card) {
                            Toast.makeText(rootView.getContext(), rootView.getContext().getString(R.string.toast_reminder_swipe_completed), Toast.LENGTH_SHORT).show();
                            Reminder reminder = reminders.get(index);
                            reminder.completed = true;
                            reminder.timeCompleted = System.currentTimeMillis();
                            dataManager.saveReminders(reminders);
                        }
                    });
                    card.setOnClickListener(new Card.OnCardClickListener() {
                        @Override
                        public void onClick(Card card, View view) {
                            Reminder reminder = reminders.get(index);
                            //Intent intent = new Intent(getActivity(), ViewEditReminderActivity.class);
                            //intent.putExtra(Constants.EXTRA_REMINDER_INDEX, index);
                            //startActivity(intent);
                        }
                    });
                    cards.add(card);

            }
        CardArrayAdapter cardArrayAdapter = new CardArrayAdapter(rootView.getContext(), cards);
        listView.setAdapter(cardArrayAdapter);
        } else {
            listView.setVisibility(View.GONE);
            noReminderLayout.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(1);

    }
}
