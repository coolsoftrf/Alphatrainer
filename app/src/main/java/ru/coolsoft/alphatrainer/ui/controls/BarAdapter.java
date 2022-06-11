package ru.coolsoft.alphatrainer.ui.controls;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

/**
* Created by Bobby© on 05.04.2015.
*/
public class BarAdapter extends ArrayAdapter<CharSequence> {
    private final int mFieldId;
    private final int mImgId;
    private final int[] mImages;
/*
    private BarAdapter(Context context, int resource, CharSequence[] objects) {
        super(context, resource, objects);
    }
*/
    private BarAdapter(Context context, int resource, int textViewResourceId, int imageViewResourceId
            , CharSequence[] objects, int[] imageArray) {
        super(context, resource, textViewResourceId, Arrays.asList(objects));
        mImages = imageArray;
        mFieldId = textViewResourceId;
        mImgId = imageViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

//if(true){ //Modify Text
        TextView text;
        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            //This should never happen as super.getView has already checked it
            Log.e(getClass().getSimpleName(), "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
        text.setVisibility(View.GONE);
//}
        //Put image
        if (mImgId != 0 && mImages != null){
            ImageView image = (ImageView) view.findViewById(mImgId);
//            image.setImageDrawable(mImages.getDrawable(position));
            image.setImageResource(mImages[position]);
        }
        return view;
    }
/*
    public static BarAdapter createFromResource(
            Context context, int textArrayResId, int textViewResId) {
        CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
        return new BarAdapter(context, textViewResId, strings);
    }*/
    public static BarAdapter createFromResource(Context context
            , int textArrayResId, int imageArrayResId, int layoutResId
            , int textViewResId, int imageViewResId) {
        Resources r = context.getResources();
        CharSequence[] strings = r.getTextArray(textArrayResId);
        TypedArray images = r.obtainTypedArray(imageArrayResId);

        int[] imageIDs = new int[strings.length];
        for (int i = 0; i < imageIDs.length; i++) {
            imageIDs[i] = images.getResourceId(i, 0);
        }
        images.recycle();

        return new BarAdapter(context, layoutResId, textViewResId, imageViewResId, strings, imageIDs);
    }
/*
    @Override
    protected void finalize() throws Throwable {
        mImages.recycle();
        super.finalize();
    }
*/
}
