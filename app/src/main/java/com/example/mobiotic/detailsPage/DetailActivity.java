package com.example.mobiotic.detailsPage;

import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mobiotic.BaseActivity;
import com.example.mobiotic.R;
import com.example.mobiotic.database.MediaDetail;
import com.example.mobiotic.database.MyDatabase;
import com.example.mobiotic.util.AppUtil;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DetailActivity extends BaseActivity implements DetailView {

    private static final String TAG = DetailActivity.class.getCanonicalName();

    private PlayerView playerView;
    private SimpleExoPlayer simpleExoPlayer;
    private ProgressBar progressBar;
    private List<MediaDetail> mediaDetails;
    private TextView title;
    private TextView description;
    private RecyclerView relatedItemsView;
    private RelatedMediaAdapter adapter;
    private DetailPresenter detailPresenter;
    private String selectItemId;
    Stack<MediaDetail> cachedStack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        selectItemId = getIntent().getStringExtra(AppUtil.ITEM_ID);
        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.progress_bar);
        title = findViewById(R.id.media_title);
        title.setText(getIntent().getStringExtra(AppUtil.TITLE));
        description = findViewById(R.id.media_description);
        description.setText(getIntent().getStringExtra(AppUtil.DESCRIPTION));
        relatedItemsView = findViewById(R.id.related_item_view);
        detailPresenter = new DetailPresenter(this, this, MyDatabase.getAppDatabase(this));
        mediaDetails = detailPresenter.getMediaBySelectedAtTop(selectItemId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        relatedItemsView.setLayoutManager(layoutManager);
        relatedItemsView.setNestedScrollingEnabled(false);
        adapter = new RelatedMediaAdapter();
        relatedItemsView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    private void initializePlayer() {
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this,
                new DefaultTrackSelector());
        detailPresenter.initialize(simpleExoPlayer);
        playerView.setPlayer(simpleExoPlayer);
        ConcatenatingMediaSource concatenatingMediaSource = detailPresenter.buildConcateMediaResource(selectItemId);
        simpleExoPlayer.prepare(concatenatingMediaSource);
        simpleExoPlayer.setPlayWhenReady(true);
        /*simpleExoPlayer.addListener(new ComponentListener());*/
        simpleExoPlayer.addAnalyticsListener(new Analytics());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || simpleExoPlayer == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        detailPresenter.updateStartPosition(simpleExoPlayer);
        simpleExoPlayer.release();
        simpleExoPlayer = null;
        playerView.setPlayer(null);
        detailPresenter.clearPlayerResource();
    }

    private void createUiForCurrentIndex(int currentWindowIndex) {
        mediaDetails = detailPresenter.getMediaBySelectedAtTop(selectItemId);
        title.setText(mediaDetails.get(currentWindowIndex).getTitle());
        description.setText(mediaDetails.get(currentWindowIndex).getDescription());
        long duration = detailPresenter.getCurrentMediaDetailDuration(currentWindowIndex);
        simpleExoPlayer.seekTo(currentWindowIndex, duration);
    }

    private class RelatedMediaAdapter extends RecyclerView.Adapter<RelatedMediaAdapter.CustomViewHolder> {

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.related_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            MediaDetail mediaDetail = mediaDetails.get(position);
            //TODO use pallete
            Glide.with(getApplicationContext())
                    .load(mediaDetail.getThumb())
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.ic_media_default))
                    .into(holder.mediaImage);
            holder.mediaTitle.setText(mediaDetail.getTitle());
            holder.mediaDescription.setText(mediaDetail.getDescription());
            if (simpleExoPlayer.getCurrentWindowIndex() == position) {

                holder.mediaImage.setVisibility(View.GONE);
                holder.mediaDescription.setVisibility(View.GONE);
                holder.mediaTitle.setVisibility(View.GONE);
                holder.container.setVisibility(View.GONE);
            } else {
                holder.mediaImage.setVisibility(View.VISIBLE);
                holder.mediaDescription.setVisibility(View.VISIBLE);
                holder.mediaTitle.setVisibility(View.VISIBLE);
                holder.container.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return mediaDetails.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final ViewGroup container;
            private ImageView mediaImage;
            private TextView mediaTitle;
            private TextView mediaDescription;

            CustomViewHolder(View itemView) {
                super(itemView);
                this.container = (ViewGroup) itemView.findViewById(R.id.root_related_item);
                this.mediaImage = itemView.findViewById(R.id.media_image);
                this.mediaTitle = itemView.findViewById(R.id.media_title);
                this.mediaDescription = itemView.findViewById(R.id.media_description);
                this.container.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                simpleExoPlayer.seekTo(this.getAdapterPosition(), simpleExoPlayer.getContentPosition());
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detailPresenter.clearViewResource();

    }

    private class Analytics implements AnalyticsListener {
        //todo find mediasources in this
        private int alreadyRemovedWindow = -2;

        @Override
        public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {
            Log.d(TAG, "onPlayerStateChanged: ");
            Timeline timeline = eventTime.timeline;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_READY:
                    progressBar.setVisibility(View.GONE);
                    break;
                case ExoPlayer.STATE_ENDED:
                    progressBar.setVisibility(View.GONE);
                    break;
                default:

                    break;
            }
        }

        @Override
        public void onTimelineChanged(EventTime eventTime, int reason) {
            detailPresenter.updateStartPosition(simpleExoPlayer);
        }

        @Override
        public void onPositionDiscontinuity(EventTime eventTime, int reason) {
            detailPresenter.updateStartPosition(simpleExoPlayer);
            Log.d(TAG, "onPositionDiscontinuity: " + reason);
        }

        @Override
        public void onSeekStarted(EventTime eventTime) {
            Log.d(TAG, "onSeekStarted: ");
        }

        @Override
        public void onSeekProcessed(EventTime eventTime) {
            Log.d(TAG, "onSeekProcessed: ");
        }

        @Override
        public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {
            Log.d(TAG, "onPlaybackParametersChanged: ");
        }

        @Override
        public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {

        }

        @Override
        public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {

        }

        @Override
        public void onLoadingChanged(EventTime eventTime, boolean isLoading) {

        }

        @Override
        public void onPlayerError(EventTime eventTime, ExoPlaybackException error) {
            Log.d(TAG, "onPlayerError: ");
            Log.d(TAG, "onPlayerError: " + error.getMessage());
        }

        @Override
        public void onTracksChanged(EventTime eventTime, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            int index = eventTime.windowIndex;
            TrackSelection trackSelection = trackSelections.get(0);
            TrackGroup trackGroup = trackSelection.getTrackGroup();
            Log.d(TAG, "onTracksChanged: ");
            createUiForCurrentIndex(simpleExoPlayer.getCurrentWindowIndex());

            adapter.notifyDataSetChanged();
        }

        @Override
        public void onLoadStarted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.d(TAG, "onLoadStarted: ");
        }

        @Override
        public void onLoadCompleted(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.d(TAG, "onLoadCompleted: ");
        }

        @Override
        public void onLoadCanceled(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData) {
            Log.d(TAG, "onLoadCanceled: ");
        }

        @Override
        public void onLoadError(EventTime eventTime, MediaSourceEventListener.LoadEventInfo loadEventInfo, MediaSourceEventListener.MediaLoadData mediaLoadData, IOException error, boolean wasCanceled) {
            Log.d(TAG, "onPlayerError: ");
            Log.d(TAG, "onPlayerError: " + error.getMessage());
        }

        @Override
        public void onDownstreamFormatChanged(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

        }

        @Override
        public void onUpstreamDiscarded(EventTime eventTime, MediaSourceEventListener.MediaLoadData mediaLoadData) {

        }

        @Override
        public void onMediaPeriodCreated(EventTime eventTime) {
            long l = eventTime.currentPlaybackPositionMs;
            Log.d(TAG, "onMediaPeriodCreated: ");
        }

        @Override
        public void onMediaPeriodReleased(EventTime eventTime) {
            long l = eventTime.currentPlaybackPositionMs;
            Log.d(TAG, "onMediaPeriodCreated: ");
        }

        @Override
        public void onReadingStarted(EventTime eventTime) {

        }

        @Override
        public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {

        }

        @Override
        public void onViewportSizeChange(EventTime eventTime, int width, int height) {

        }

        @Override
        public void onNetworkTypeChanged(EventTime eventTime, @Nullable NetworkInfo networkInfo) {
            Log.d(TAG, "onNetworkTypeChanged: ");
            if (networkInfo.isConnected()) {
                Toast.makeText(getApplicationContext(), "Network enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Network disabled", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onMetadata(EventTime eventTime, Metadata metadata) {

        }

        @Override
        public void onDecoderEnabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

        }

        @Override
        public void onDecoderInitialized(EventTime eventTime, int trackType, String decoderName, long initializationDurationMs) {

        }

        @Override
        public void onDecoderInputFormatChanged(EventTime eventTime, int trackType, Format format) {

        }

        @Override
        public void onDecoderDisabled(EventTime eventTime, int trackType, DecoderCounters decoderCounters) {

        }

        @Override
        public void onAudioSessionId(EventTime eventTime, int audioSessionId) {

        }

        @Override
        public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(EventTime eventTime, int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(EventTime eventTime, Surface surface) {

        }

        @Override
        public void onDrmKeysLoaded(EventTime eventTime) {

        }

        @Override
        public void onDrmSessionManagerError(EventTime eventTime, Exception error) {

        }

        @Override
        public void onDrmKeysRestored(EventTime eventTime) {

        }

        @Override
        public void onDrmKeysRemoved(EventTime eventTime) {

        }
    }
}
