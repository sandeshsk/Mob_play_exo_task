package com.example.mobiotic.listingPage;

import com.example.mobiotic.BaseView;
import com.example.mobiotic.database.MediaDetail;

import java.util.List;

public interface ListingView extends BaseView {

    void setMediaDetailList(List<MediaDetail> mediaList);

}
