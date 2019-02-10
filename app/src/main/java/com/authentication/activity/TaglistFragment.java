package com.authentication.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TaglistFragment extends ListFragment {
	private List<String> presidents = new ArrayList<String>();
	public MyAdapter myadapter = null;
	private int curSelPosition = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.taglist, container, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myadapter = new MyAdapter(getActivity());
		setListAdapter(myadapter);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getListView().setVerticalScrollBarEnabled(true);
	}

	public void onListItemClick(ListView parent, View v, int position, long id) {
		// 记录当前选中的标签位置
		curSelPosition = position;

		// 更新读写窗口的标签信息
		final FragmentManager fragmentManager = getActivity()
				.getFragmentManager();

		TagReadFragment objFragment = (TagReadFragment) fragmentManager
				.findFragmentById(R.id.fragment_tagRead);
		TagWriteFragment objWriteFragment = (TagWriteFragment) fragmentManager
				.findFragmentById(R.id.fragment_tagWrite);

		TextView txtEpc = (TextView) objFragment.getActivity().findViewById(
				R.id.txtReadEpc);
		TextView txtWriteEpc = (TextView) objWriteFragment.getActivity()
				.findViewById(R.id.txtWriteEpc);

		txtEpc.setText(presidents.get(position));
		txtWriteEpc.setText(presidents.get(position));
		/*
		 * Toast.makeText(getActivity(), "You have selected " +
		 * presidents.get(position), Toast.LENGTH_SHORT) .show();
		 */
	}

	/**
	 * 增加列表显示项
	 * 
	 * @param tagEPC
	 *            要显示的EPC信息
	 */
	public void addItem(String tagEPC) {
		presidents.add(tagEPC);
		myadapter.notifyDataSetChanged();
	}

	/**
	 * 清除列表的显示内容
	 */
	public void clearItem() {
		presidents.clear();

		final FragmentManager fragmentManager = getActivity()
				.getFragmentManager();

		TagReadFragment objFragment = (TagReadFragment) fragmentManager
				.findFragmentById(R.id.fragment_tagRead);
		TagWriteFragment objWriteFragment = (TagWriteFragment) fragmentManager
				.findFragmentById(R.id.fragment_tagWrite);

		TextView txtEpc = (TextView) objFragment.getActivity().findViewById(
				R.id.txtReadEpc);
		TextView txtWriteEpc = (TextView) objWriteFragment.getActivity()
				.findViewById(R.id.txtWriteEpc);

		txtEpc.setText(getText(R.string.txt_null));
		txtWriteEpc.setText(getText(R.string.txt_null));

		myadapter.notifyDataSetChanged();
	}

	/**
	 * 更新当前被选中的项目信息
	 * 
	 * @param tagEPC
	 */
	public void updateSelItem(String tagEPC) {
		if (curSelPosition != -1) {
			presidents.set(curSelPosition, tagEPC);
			myadapter.notifyDataSetChanged();
		}
	}

	class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater = null;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return presidents.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null)
            {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.datalist, null);
                holder.epc = (TextView)convertView.findViewById(R.id.epcId);
                holder.time = (TextView)convertView.findViewById(R.id.readNum);
                convertView.setTag(holder);
            }else
            {
                holder = (ViewHolder)convertView.getTag();
            }
            String epc = presidents.get(position);
            holder.epc.setText(epc);
            holder.time.setText( ""+((BaseUHFActivity)getActivity()).number.get(epc));
			return convertView;
		}
		
	     class ViewHolder
	    {
	        public TextView epc;
	        public TextView time;
	    }

	}
}
