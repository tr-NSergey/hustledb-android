package ru.hustledb.hustledb;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.hustledb.hustledb.Events.OnPreregistrationLoadCompleteEvent;
import ru.hustledb.hustledb.ValueClasses.Contest;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

public class ContestDetailsFragment extends Fragment implements RecyclerView.OnItemTouchListener {

    private Contest contest;
    private CompositeSubscription subscriptions;
    private ContestsListFragment.CompetitionsListener listener;
    private RecyclerView.LayoutManager layoutManager;
    private PreregistrationRecyclerAdapter recyclerAdapter;
    private GestureDetectorCompat gestureDetector;

//    @BindView(R.id.dtSwipeRefreshLayout)
//    SwipeRefreshLayout dtSwipeRefreshLayout;
    @BindView(R.id.dtTitle)
    TextView dtTitle;
    @BindView(R.id.dtCity)
    TextView dtCity;
    @BindView(R.id.dtDate)
    TextView dtDate;
    @BindView(R.id.dtDesc)
    TextView dtDesc;
    @BindView(R.id.dtListTitle)
    TextView dtListTitle;
    @BindView(R.id.dtRecyclerView)
    RecyclerView dtRecyclerView;
    @Inject
    ContestsCache contestsCache;
    @Inject
    PreregistrationCache preregistrationCache;
    @Inject
    RxBus bus;

    public ContestDetailsFragment() {
        // Required empty public constructor
    }
    public static ContestDetailsFragment newInstance() {
        return new ContestDetailsFragment();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof ContestsListFragment.CompetitionsListener)) {
            throw new IllegalArgumentException("Parent activity must implement" + ContestsListFragment.CompetitionsListener.class);
        }
        listener = (ContestsListFragment.CompetitionsListener) context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent().inject(this);
        contest = ContestsCache.getInstance().getSelectedContest();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_competition_details, container, false);
        ButterKnife.bind(this, view);
//        displayPreregistrationInfo.setOnClickListener(v -> listener.onPreregistrationInfoClicked(contest.getId()));
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        layoutManager = new LinearLayoutManager(getActivity());
        dtRecyclerView.setLayoutManager(layoutManager);
        dtRecyclerView.setHasFixedSize(true);
        recyclerAdapter = new PreregistrationRecyclerAdapter(null);
        dtRecyclerView.setAdapter(recyclerAdapter);
        dtRecyclerView.setItemAnimator(new DefaultItemAnimator());
        dtRecyclerView.addOnItemTouchListener(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(dtRecyclerView.getContext(),
                LinearLayout.VERTICAL);
        dtRecyclerView.addItemDecoration(dividerItemDecoration);
        gestureDetector = new GestureDetectorCompat(getActivity(), new ContestDetailsFragment.RecyclerViewOnGestureListener());
    }
    @Override
    public void onResume() {
        super.onResume();
        preregistrationCache.registerPreregistrationObserver(recyclerAdapter);
        subscriptions = new CompositeSubscription();
        subscriptions.add(bus.asObservable()
                .subscribe(o -> {
                    if (o instanceof OnPreregistrationLoadCompleteEvent) {
                        onLoadComplete((OnPreregistrationLoadCompleteEvent) o);
                    }
                })
        );
        if(!contest.getTitle().equals("")) {
            ((View) dtTitle.getParent()).setVisibility(View.VISIBLE);
//            if(!contest.getUrl().equals("")){
//                dtTitle.setMovementMethod(LinkMovementMethod.getInstance());
//                String link = "<a href=" + contest.getUrl() + ">" +
//                        contest.getTitle() + "</a>";
//                dtTitle.setText(Html.fromHtml(link));
//            } else {
                dtTitle.setText(contest.getTitle());
//            }
        } else {
            ((View) dtTitle.getParent()).setVisibility(GONE);
        }
        if(!contest.getCityName().equals("")){
            dtCity.setText(contest.getCityName());
            ((View) dtCity.getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) dtCity.getParent()).setVisibility(GONE);
        }
        if(!contest.getDate().equals("")) {
            dtDate.setText(contest.getPrettyDate());
            ((View) dtDate.getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) dtDate.getParent()).setVisibility(GONE);
        }
        if(!contest.getCommonInfo().equals("")) {
            dtDesc.setText(contest.getCommonInfo());
            ((View) dtDesc.getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) dtDesc.getParent()).setVisibility(GONE);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        recyclerAdapter.onNext(null);
        dtListTitle.setText("");
        preregistrationCache.unregisterPreregistrationObserver(recyclerAdapter);
        subscriptions.unsubscribe();
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
    public void onLoadComplete(OnPreregistrationLoadCompleteEvent event) {
//        swipeRefreshLayout.setRefreshing(false);
        if (event.isError()) {
            dtListTitle.setText("Информация о пререгистрации недоступна");
            //Toast.makeText(getContext(), "Произошла ошибка загрузки", Toast.LENGTH_SHORT).show();
        } else{
            dtListTitle.setText("Номинации и регистрации");
        }
    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            View view = dtRecyclerView.findChildViewUnder(event.getX(), event.getY());
            if(view != null) {
                preregistrationCache.selectNomination(((TextView)view.findViewById(R.id.piClas)).getText().toString());
                listener.onNominationClicked();
            }
            return super.onSingleTapConfirmed(event);
        }

        @Override
        public void onLongPress(MotionEvent event) {
            super.onLongPress(event);
        }
    }
}