package com.example.mobiotic.detailsPage;

import android.net.Uri;

import com.example.mobiotic.database.MediaDetail;
import com.example.mobiotic.database.MediaDetailDao;
import com.example.mobiotic.database.MyDatabase;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.util.List;

public class DetailPresenter {

    private final DetailActivity activity;
    private DetailView detailView;
    private MediaDetailDao mediaDetailDao;
    private SimpleExoPlayer simpleExoPlayer;
    private SimpleCache cache;
    private static final long MAX_CACHE_SIZE = 1024 * 1024 * 60;
    private List<MediaDetail> mediaDetails;

    public DetailPresenter(DetailActivity activity, DetailView detailView,
                           MyDatabase myDatabase) {
        this.activity = activity;
        this.detailView = detailView;
        this.mediaDetailDao = myDatabase.getMediaDetailDao();
    }

    public List<MediaDetail> getMediaBySelectedAtTop(String id) {
        if (mediaDetails == null) {
            mediaDetails = mediaDetailDao.getMediaWithTopSelected(id);
        }
        return mediaDetails;
    }

    public MediaDetail getMediaDetailById(String id) {
        return mediaDetailDao.chooseMediaDetailById(id);
    }

    public void updateStartPosition(SimpleExoPlayer simpleExoPlayer) {
        int index = simpleExoPlayer.getCurrentWindowIndex();
        long duration = 0;
        if (simpleExoPlayer.getContentPosition() < simpleExoPlayer.getDuration()) {
            duration = Math.max(0, simpleExoPlayer.getContentPosition());
        }
        if (mediaDetails != null && !mediaDetails.isEmpty()) {
            mediaDetails.get(index).setLastPlayedPostion(duration);
        }
    }

    public void initialize(SimpleExoPlayer simpleExoPlayer) {
        this.simpleExoPlayer = simpleExoPlayer;
    }

    public ConcatenatingMediaSource buildConcateMediaResource(String id) {
        List<MediaDetail> mediaDetails = getMediaBySelectedAtTop(id);
        this.cache = new SimpleCache(activity.getCacheDir(), new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE));

        HttpDataSource.Factory factory = new DefaultHttpDataSourceFactory("mobiotic");
        DataSource.Factory dataSourceFactory = new CacheDataSourceFactory(cache, factory);
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (MediaDetail mediaDetail : mediaDetails) {
            ExtractorMediaSource extractorMediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mediaDetail.getUrl()));
            concatenatingMediaSource.addMediaSource(extractorMediaSource);
        }
        return concatenatingMediaSource;
    }

    public long getCurrentMediaDetailDuration(int index) {
        return mediaDetailDao.getDurationOfPerticularMedia(mediaDetails.get(index).getId());
    }

    public void clearPlayerResource() {
        mediaDetailDao.insertMediaDetails(mediaDetails);
        try {
            cache.release();
        } catch (Cache.CacheException e) {
            e.printStackTrace();
        }
    }

    public void clearViewResource() {
        detailView = null;
    }

    public boolean checkViewPresent() {
        return detailView != null;
    }

    public MediaDetail addAlreadyRemovedItem(int currentWindowIndex) {
        return mediaDetails.get(currentWindowIndex);
    }
}
