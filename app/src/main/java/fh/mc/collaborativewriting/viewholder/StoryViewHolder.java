package fh.mc.collaborativewriting.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import fh.mc.collaborativewriting.R;
import fh.mc.collaborativewriting.models.Story;

/**
 * Created by Mark on 11.06.2016.
 */
public class StoryViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView descriptionView;
    public TextView authorView;
    public TextView numberOfStarsView;
    public ImageView starView;


    public StoryViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.story_title);
        descriptionView = (TextView) itemView.findViewById(R.id.story_description);
        authorView = (TextView) itemView.findViewById(R.id.story_author);
        numberOfStarsView = (TextView) itemView.findViewById(R.id.story_num_stars);
        starView = (ImageView) itemView.findViewById(R.id.star);
    }

    public void bindToStory(Story story, View.OnClickListener starClickListener) {
        titleView.setText(story.title);
        authorView.setText(story.author);
        numberOfStarsView.setText(String.valueOf(story.starCount));
        descriptionView.setText(story.body);

        starView.setOnClickListener(starClickListener);
    }
}
