package com.example.mobiotic.listingPage;

import com.example.mobiotic.R;
import com.example.mobiotic.database.MediaDetail;
import com.example.mobiotic.database.MediaDetailDao;
import com.example.mobiotic.database.MyDatabase;
import com.example.mobiotic.network.ApiCaller;
import com.example.mobiotic.network.ServiceApi;

import java.util.HashMap;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ListingPresenter {

    private final MyDatabase myDatabase;
    private ListingActivity activity;
    private ListingView listingView;
    private List<MediaDetail> mediaDetails;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ConstraintLayout rootLayout;

    public ListingPresenter(ListingActivity activity,
                            ListingView listingView, ConstraintLayout rootLayout) {
        this.rootLayout = rootLayout;
        this.listingView = listingView;
        this.activity = activity;
        this.myDatabase = MyDatabase.getAppDatabase(activity.getApplicationContext());
        MediaDetailDao mediaDetailDao = myDatabase.getMediaDetailDao();
        mediaDetails = mediaDetailDao.chooseAllMediaDetails();
    }

    public void getMediaDetailsFromRepo() {
        if (mediaDetails != null && !mediaDetails.isEmpty()) {
            listingView.setMediaDetailList(mediaDetails);
        } else {
            callToServer();
        }

    }

    private void callToServer() {
        listingView.showProgressDialog();
        HashMap<String, String> request = new HashMap<>();
        request.put("print", "pretty");
        ServiceApi serviceApi = ApiCaller.getInstance().getServicesApi();

        compositeDisposable.add(serviceApi.fetchMediaFromServer(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<List<MediaDetail>>() {
                    @Override
                    public void onSuccess(List<MediaDetail> mediaDetailsList) {
                        if (isViewAttached()) {
                            myDatabase.getMediaDetailDao().insertMediaDetails(mediaDetailsList);
                            listingView.hideProgressDialog();
                            listingView.setMediaDetailList(mediaDetailsList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (isViewAttached()) {
                            listingView.hideProgressDialog();
                            listingView.showError(rootLayout,
                                    activity.getString(R.string.server_connections_problem_try_again));
                        }
                    }
                }));
    }

    private boolean isViewAttached() {
        return listingView != null;
    }

    public void clearResource() {
        compositeDisposable.clear();
        if (listingView != null) {
            listingView = null;
        }
    }

}

