package com.example.mobiotic.listingPage;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mobiotic.BaseActivity;
import com.example.mobiotic.R;
import com.example.mobiotic.database.MediaDetail;
import com.example.mobiotic.detailsPage.DetailActivity;
import com.example.mobiotic.util.AppUtil;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListingActivity extends BaseActivity implements ListingView {
    private RecyclerView listingView;
    private List<MediaDetail> mediaList;
    private ConstraintLayout rootLayout;
    private ListingPresenter listingPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        listingView = findViewById(R.id.listing_view);
        rootLayout = findViewById(R.id.root_layout);
        listingPresenter = new ListingPresenter(this, this, rootLayout);
        listingPresenter.getMediaDetailsFromRepo();
    }

    @Override
    public void setMediaDetailList(List<MediaDetail> mediaList) {
        this.mediaList = mediaList;
        listingView.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listingView.setLayoutManager(layoutManager);
        MediaListAdapter adapter = new MediaListAdapter();
        listingView.setAdapter(adapter);
    }


    private class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.CustomViewHolder> {

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.media_detail_list_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            MediaDetail mediaDetail = mediaList.get(position);
            //TODO use pallete remove centercrop
            Glide.with(getApplicationContext())
                    .load(mediaDetail.getThumb())
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.ic_media_default))
                    .into(holder.mediaImage);
            holder.mediaTitle.setText(mediaDetail.getTitle());
            holder.mediaDescription.setText(mediaDetail.getDescription());

        }

        @Override
        public int getItemCount() {
            return mediaList.size();
        }

        class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final ViewGroup container;
            private ImageView mediaImage;
            private TextView mediaTitle;
            private TextView mediaDescription;

            public CustomViewHolder(View itemView) {
                super(itemView);
                this.container = (ViewGroup) itemView.findViewById(R.id.root_media_list_item);
                this.mediaImage = itemView.findViewById(R.id.media_image);
                this.mediaTitle = itemView.findViewById(R.id.media_title);
                this.mediaDescription = itemView.findViewById(R.id.media_description);
                this.container.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                MediaDetail mediaDetail = mediaList.get(
                        this.getAdapterPosition()
                );
                startDetailActivity(mediaDetail, v);

            }
        }

    }

    public void startDetailActivity(MediaDetail mediaDetail, View viewRoot) {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra(AppUtil.ITEM_ID, mediaDetail.getId());
        i.putExtra(AppUtil.TITLE, mediaDetail.getTitle());
        i.putExtra(AppUtil.DESCRIPTION, mediaDetail.getDescription());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade(Fade.IN));
            getWindow().setEnterTransition(new Fade(Fade.OUT));
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this,
                            new Pair<>(viewRoot.findViewById(R.id.media_image),
                                    getString(R.string.transition_image)),
                            new Pair<>(viewRoot.findViewById(R.id.media_title),
                                    getString(R.string.transition_title)));

            startActivity(i, options.toBundle());

        } else {
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listingPresenter.clearResource();
    }
}
