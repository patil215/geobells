package com.patil.geobells.lite.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.patil.geobells.lite.MainActivity;
import com.patil.geobells.lite.R;
import com.patil.geobells.lite.ViewReminderActivity;
import com.patil.geobells.lite.data.Reminder;
import com.patil.geobells.lite.service.LocationService;
import com.patil.geobells.lite.utils.Constants;
import com.patil.geobells.lite.utils.GeobellsDataManager;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class CompletedRemindersFragment extends Fragment {

    private CardListView listView;
    private CardArrayAdapter cardArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_completed, container, false);
        listView = (CardListView) rootView.findViewById(R.id.cardList);
        RelativeLayout noReminderLayout = (RelativeLayout) rootView.findViewById(R.id.layout_noreminders);
        final GeobellsDataManager dataManager = new GeobellsDataManager(rootView.getContext());
        final ArrayList<Reminder> reminders = dataManager.getSavedReminders();
        if(numCompletedReminders(reminders) > 0) {
            listView.setVisibility(View.VISIBLE);
            noReminderLayout.setVisibility(View.GONE);
            ArrayList<Card> cards = new ArrayList<Card>();
            for (int i = 0; i < reminders.size(); i++) {
                if(reminders.get(i).completed) {
                    final int index = i;
                    if (reminders.get(i).completed) {
                        ReminderCard card = new ReminderCard(rootView.getContext(), R.layout.card_reminder, reminders.get(i), i);
                        card.setSwipeable(true);
                        card.setOnSwipeListener(new Card.OnSwipeListener() {
                            @Override
                            public void onSwipe(Card card) {
                                Toast.makeText(rootView.getContext(), rootView.getContext().getString(R.string.toast_reminder_swipe_uncompleted), Toast.LENGTH_SHORT).show();
                                Reminder reminder = reminders.get(index);
                                reminder.completed = false;
                                dataManager.saveReminders(reminders);
                                getActivity().stopService(new Intent(getActivity(), LocationService.class));
                                getActivity().startService(new Intent(getActivity(), LocationService.class));
                            }
                        });
                        card.setOnClickListener(new Card.OnCardClickListener() {
                            @Override
                            public void onClick(Card card, View view) {
                                Intent intent = new Intent(getActivity(), ViewReminderActivity.class);
                                intent.putExtra(Constants.EXTRA_REMINDER_INDEX, index);
                                startActivityForResult(intent, 12);
                            }
                        });
                        cards.add(card);
                    }
                }
            }
            cardArrayAdapter = new CardArrayAdapter(rootView.getContext(), cards);
            listView.setAdapter(cardArrayAdapter);
        } else {
            listView.setVisibility(View.GONE);
            noReminderLayout.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((MainActivity)getActivity()).refreshCompleted();
    }

    public int numCompletedReminders(ArrayList<Reminder> reminders) {
        int sum = 0;
        for(Reminder reminder : reminders) {
            if(reminder.completed) {
                sum++;
            }
        }
        return sum;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(2);

    }
}
