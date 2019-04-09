package sysproj.seonjoon.iot_personal_projectapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class PictureListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> filenameList;

    public PictureListAdapter(Context context, ArrayList<String> items)
    {
        this.context = context;
        filenameList = items;
    }

    @Override
    public int getCount() {
        return filenameList.size();
    }

    @Override
    public Object getItem(int i) {
        return filenameList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.picture_list_custom_layout, viewGroup, false);

        TextView label = (TextView) view.findViewById(R.id.picture_custom_label);
        ImageView imageView = (ImageView) view.findViewById(R.id.picture_custom_image);

        String fileName = filenameList.get(i);

        if (fileName.endsWith(".jpg"))
            imageView.setImageResource(R.drawable.ic_picture);
        else
            imageView.setImageResource(R.drawable.ic_movie);

        label.setText(filenameList.get(i));

        return view;
    }
}
