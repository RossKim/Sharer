package com.ross.kbs.sharer.models;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ross.kbs.sharer.R;

public class AccountListViewAdapter extends ArrayAdapter<AccountItem> {

	Context context;

	public AccountListViewAdapter(Context context, int resource,
			List<AccountItem> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	private class ViewHolder {
		ImageView itemImageView;
		TextView itemTextView;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		AccountItem item = getItem(position);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.account_list_view_item,
					null);
			holder = new ViewHolder();
			holder.itemTextView = (TextView) convertView
					.findViewById(R.id.itemText);
			holder.itemImageView = (ImageView) convertView
					.findViewById(R.id.itemImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.itemTextView.setText(item.getItemText());
		holder.itemImageView.setImageResource(item.getItemImageId());

		return convertView;
	}

}
