package hu.ait.placesaver.adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.Collections;
import java.util.List;

import hu.ait.placesaver.MainActivity;
import hu.ait.placesaver.R;
import hu.ait.placesaver.data.Place;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvLocTitle;
        public TextView tvLocDate;
        public ImageView ivLocImg;
        public Button btnDetails;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLocTitle = (TextView) itemView.findViewById(R.id.tvLocTitle);
            tvLocDate = (TextView) itemView.findViewById(R.id.tvLocDate);
            ivLocImg = (ImageView) itemView.findViewById(R.id.ivLocImg);
            btnDetails = (Button) itemView.findViewById(R.id.btnDetails);
        }
    }

    private List<Place> placesList;
    private Context context;
    private int lastPosition = -1;



    public PlacesAdapter(List<Place> placesList, Context context) {
        this.placesList = placesList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.map_thumbnail_layout, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.tvLocTitle.setText(placesList.get(position).getLocTitle());
        viewHolder.tvLocDate.setText(placesList.get(position).getLocDate());

        Glide.with((MainActivity)context).load(placesList.get(position).getPlacePictureURL()).into(viewHolder.ivLocImg);

        viewHolder.btnDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).showEditPlaceActivity(
                        placesList.get(viewHolder.getAdapterPosition()).getPlaceID(),
                        viewHolder.getAdapterPosition());
            }
        });

        setAnimation(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public void addPlace(Place place) {
        placesList.add(place);
        notifyDataSetChanged();
    }

    public void updatePlace(int index, Place place) {
        placesList.set(index, place);

        notifyItemChanged(index);
    }

    public void removePlace(int index) {
        ((MainActivity)context).deletePlace(placesList.get(index));
        placesList.remove(index);
        notifyItemRemoved(index);
    }



    public void removePlaceByKey(String key) {
        for (int i = 0; i < placesList.size(); i++) {
            if (placesList.get(i).getPlaceID().equals(key)) {
                placesList.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void swapPlaces(int oldPosition, int newPosition) {
        if (oldPosition < newPosition) {
            for (int i = oldPosition; i < newPosition; i++) {
                Collections.swap(placesList, i, i + 1);
            }
        } else {
            for (int i = oldPosition; i > newPosition; i--) {
                Collections.swap(placesList, i, i - 1);
            }
        }
        notifyItemMoved(oldPosition, newPosition);
    }

    public Place getPlace(int i) {
        return placesList.get(i);
    }

    public void sortByName(){
        Collections.sort(placesList, new Place.NameCompare());
        notifyDataSetChanged();
    }

    public void sortByDate() {
        Collections.sort(placesList, new Place.DateCompare());
        notifyDataSetChanged();
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

}
