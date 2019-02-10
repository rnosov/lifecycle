package com.authentication.activity;

import java.util.ArrayList;
import java.util.List;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android_serialport_api.BeidouAPI;
import android_serialport_api.BeidouAPI.OnUpdateDataListener;
import android_serialport_api.BeidouAPI.Satellite;

public class LocationActivity extends BaseActivity implements
		OnPageChangeListener {
	private final Logger logger = LoggerFactory.getLogger();
	private MyHandler myHandler;

	private BeidouAPI api;

	private Spinner spinner1;
	private Spinner spinner2;
	private Spinner spinner3;
	private TextView modeText;

	private ArrayAdapter<String> adapter1;
	private ArrayAdapter<String> adapter2;
	private ArrayAdapter<String> adapter3;

	private static final String[] START_CONTROL_COMMAND = new String[] {
			"模块冷启动命令", "模块温启动命令", "模块热启动命令" };

	private static final String[] MODE_SWITCH_COMMAND = new String[] {
			"使用GPS+BDS 混星模式操作", "使用纯GPS 模式操作", "使用纯BDS 模式操作" };

	private static final String[] MODE_TITLE = new String[] { "GPS+BDS 混星模式",
			"纯GPS 模式", "纯BDS 模式" };

	private static final String[] STATEMENT_FREQUENCY_COMMAND = new String[] {
			"模块只输出GLL 语句", "模块只输出RMC 语句", "模块只输出VTG 语句", "模块只输出GGA 语句",
			"模块只输出GSA 语句", "模块只输出GSV 语句", "模块只输出ZDA 语句",
			"模块恢复为输出RMC，GGA，GSA，GSV（5 秒输出一次），GLL，VTG，ZDA" };

	private static final byte[][] command1 = { BeidouAPI.COLD_START_COMMAND,
			BeidouAPI.WARM_START_COMMAND, BeidouAPI.HOT_START_COMMAND };

	private static final byte[][] command2 = {
			BeidouAPI.GPS_BDS_MODE_OPERATION_COMMAND,
			BeidouAPI.GPS_MODE_OPERATION_COMMAND,
			BeidouAPI.BDS_MODE_OPERATION_COMMAND };

	private static final byte[][] command3 = {
			BeidouAPI.GLL_STATEMENT_MODEL_COMMAND,
			BeidouAPI.RMC_STATEMENT_MODEL_COMMAND,
			BeidouAPI.VTG_STATEMENT_MODEL_COMMAND,
			BeidouAPI.GGA_STATEMENT_MODEL_COMMAND,
			BeidouAPI.GSA_STATEMENT_MODEL_COMMAND,
			BeidouAPI.GSV_STATEMENT_MODEL_COMMAND,
			BeidouAPI.ZDA_STATEMENT_MODEL_COMMAND,
			BeidouAPI.RMC_GGA_GSA_GSV_STATEMENT_MODEL_COMMAND };

	private boolean isStart1 = true;
	private boolean isStart2 = true;
	private boolean isStart3 = true;

	private String[][] keys = new String[][] {
			// GGA
			{ "UTC Time", "Latitude", "N/S Indicator", "Longitude",
					"E/W Indicator", "Position Fix Indicator",
					"Satellites Used", "HOOP", "MSL Altitude", "Units",
					"Geoid Separation", "Units", "Age of Diff. Corr.",
					"Diff. Ref. Station ID" },
			// GLL
			{ "Latitude", "Indicator_N_S", "Longitude", "Indicator_E_W", "UTC",
					"location" },
			// GSA
			{ "mode1", "mode2", "PDOP", "HDOP", "VDOP" },
			// GSV
			{ "TotalNumberOfMessage", "MessageNumber", "SatelliteInView" },
			// RMC
			{ "UTC", "Status", "Latitude", "Indicator_N_S", "Longitude",
					"Indicator_E_W", "SpeedOverGround", "CourseOverGround",
					"Date", "MagneticVariation", "VariationSense", "Mode" },
			// VTG
			{ "CourseOverGround1", "Reference1", "CourseOverGround2",
					"Reference2", "SpeedOverGround1", "Units1",
					"SpeedOverGround2", "Units2", "Mode" } };

	ViewPager mvp;
	ImageView[] images;
	ImageView iv;
	LinearLayout ll;
	ViewGroup vg;

	View ggaView;
	View gllView;
	View gsaView;
	View gsvView;
	View rmcView;
	View vtgView;
	private ListView ggaListView;
	private ListView gllListView;
	private ListView gsaListView;
	private ListView gsa2ListView;
	private ListView gsvListView;
	private ListView gsv2ListView;
	private ListView rmcListView;
	private ListView vtgListView;
	private ListView[] listViews;
	
	
	private ListView satellite1;
	private ListView satellite2;
	
	private ListView prn1;
	private ListView prn2;
	
	
	
	private MyAdapter locationAdapter1;
	private MyAdapter locationAdapter2;
	private SatelliteAdapter satelliteAdapter1;
	private SatelliteAdapter satelliteAdapter2;
	private PrnAdapter prnAdapter1;
	private PrnAdapter prnAdapter2;
	private ArrayList<String> ggaList = new ArrayList<String>();
	private ArrayList<String> gllList = new ArrayList<String>();
	private ArrayList<String> gsaList = new ArrayList<String>();
	private ArrayList<String> gsa2List = new ArrayList<String>();
	private ArrayList<String> gsvList = new ArrayList<String>();
	private ArrayList<String> gsv2List = new ArrayList<String>();
	private ArrayList<String> rmcList = new ArrayList<String>();
	private ArrayList<String> vtgList = new ArrayList<String>();
	private ArrayList<ArrayList<String>> list1 = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> list2 = new ArrayList<ArrayList<String>>();
	private WakeLock wakeLock =null;
	private void acquireWakeLock() {
        if (wakeLock ==null) {
               PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
               wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
               wakeLock.acquire();
           }
       
   }


private void releaseWakeLock() {
       if (wakeLock !=null&& wakeLock.isHeld()) {
           wakeLock.release();
           wakeLock =null;
       }

   }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i("whw", "onCreate  =" + this.toString());
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		PropertyConfigurator.getConfigurator(this).configure();
		final FileAppender  fa =  (FileAppender) logger.getAppender(1);  
		fa.setAppend(true); 
		logger.debug("**********Enter Myapplication********");
		initView();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		api = new BeidouAPI(logger);
		api.setOnUpdateDataListener(new OnUpdateDataListener() {

			@Override
			public void VTG(int mode, String CourseOverGround1,
					String Reference1, String CourseOverGround2,
					String Reference2, String SpeedOverGround1, String Units1,
					String SpeedOverGround2, String Units2, String Mode) {
				vtgList.clear();
				vtgList.add(CourseOverGround1);
				vtgList.add(Reference1);
				vtgList.add(CourseOverGround2);
				vtgList.add(Reference2);
				vtgList.add(SpeedOverGround1);
				vtgList.add(Units1);
				vtgList.add(SpeedOverGround2);
				vtgList.add(Units2);
				vtgList.add(Mode);
				if (selectIndex == 5) {
					locationAdapter1.notifyDataSetChanged();
				}

			}

			@Override
			public void RMC(int mode, String UTC, String Status,
					String Latitude, String Indicator_N_S, String Longitude,
					String Indicator_E_W, String SpeedOverGround,
					String CourseOverGround, String Date,
					String MagneticVariation, String VariationSense, String Mode) {
				rmcList.clear();
				rmcList.add(UTC);
				rmcList.add(Status);
				rmcList.add(Latitude);
				rmcList.add(Indicator_N_S);
				rmcList.add(Longitude);
				rmcList.add(Indicator_E_W);
				rmcList.add(SpeedOverGround);
				rmcList.add(CourseOverGround);
				rmcList.add(Date);
				rmcList.add(MagneticVariation);
				rmcList.add(VariationSense);
				rmcList.add(Mode);
				if (selectIndex == 4) {
					locationAdapter1.notifyDataSetChanged();
				}
			}

			@Override
			public void GSV(int mode, String TotalNumberOfMessage,
					String MessageNumber, String SatelliteInView,
					List<Satellite> satelliteList) {
				if (mode == BeidouAPI.MODE_GPS) {
					gsvList.clear();
					gsvList.add(TotalNumberOfMessage);
					gsvList.add(MessageNumber);
					gsvList.add(SatelliteInView);
					satelliteAdapter1.setList(satelliteList);
					Log.i("whw", "satelliteList1 size="+satelliteList.size());
					if (selectIndex == 3) {
						locationAdapter1.notifyDataSetChanged();
						satelliteAdapter1.notifyDataSetChanged();
					}
				} else if (mode == BeidouAPI.MODE_BEIDOU) {
					gsv2List.clear();
					gsv2List.add(TotalNumberOfMessage);
					gsv2List.add(MessageNumber);
					gsv2List.add(SatelliteInView);
					satelliteAdapter2.setList(satelliteList);
					Log.i("whw", "satelliteList2 size="+satelliteList.size());
					if (selectIndex == 3) {
						locationAdapter2.notifyDataSetChanged();
						satelliteAdapter2.notifyDataSetChanged();
					}
				}

			}

			@Override
			public void GSA(int mode, String mode1, String mode2,
					List<String> IDOfSatelliteUsedList, String PDOP,
					String HDOP, String VDOP) {
				if (mode == BeidouAPI.MODE_GPS) {
					gsaList.clear();
					gsaList.add(mode1);
					gsaList.add(mode2);
					gsaList.add(PDOP);
					gsaList.add(HDOP);
					gsaList.add(VDOP);
					prnAdapter1.setList(IDOfSatelliteUsedList);
					if (selectIndex == 2) {
						locationAdapter1.notifyDataSetChanged();
						prnAdapter1.notifyDataSetChanged();
					}
				} else if (mode == BeidouAPI.MODE_BEIDOU) {
					gsa2List.clear();
					gsa2List.add(mode1);
					gsa2List.add(mode2);
					gsa2List.add(PDOP);
					gsa2List.add(HDOP);
					gsa2List.add(VDOP);
					prnAdapter2.setList(IDOfSatelliteUsedList);
					if (selectIndex == 2) {
						locationAdapter2.notifyDataSetChanged();
						prnAdapter2.notifyDataSetChanged();
					}
				}

			}

			@Override
			public void GLL(int mode, String Latitude, String Indicator_N_S,
					String Longitude, String Indicator_E_W, String UTC,
					String location) {
				gllList.clear();
				gllList.add(Latitude);
				gllList.add(Indicator_N_S);
				gllList.add(Longitude);
				gllList.add(Indicator_E_W);
				gllList.add(UTC);
				gllList.add(location);
				if (selectIndex == 1) {
					locationAdapter1.notifyDataSetChanged();
				}

			}

			@Override
			public void GGA(int mode, String UTC, String Latitude,
					String Indicator_N_S, String Longitude,
					String Indicator_E_W, String PositionFixIndicator,
					String satellitesUsed, String HDOP, String MSLAltitude,
					String units1, String GeoidSeparation, String units2,
					String AgeOfDiffCorr, String DiffRefStationID) {
				ggaList.clear();
				ggaList.add(UTC);
				ggaList.add(Latitude);
				ggaList.add(Indicator_N_S);
				ggaList.add(Longitude);
				ggaList.add(Indicator_E_W);
				ggaList.add(PositionFixIndicator);
				ggaList.add(satellitesUsed);
				ggaList.add(HDOP);
				ggaList.add(MSLAltitude);
				ggaList.add(units1);
				ggaList.add(GeoidSeparation);
				ggaList.add(units2);
				ggaList.add(AgeOfDiffCorr);
				ggaList.add(DiffRefStationID);
				if (selectIndex == 0) {
					locationAdapter1.notifyDataSetChanged();
				}
			}
		});
		api.open();
		acquireWakeLock();
	}
	
	

	@Override
	protected void onPause() {
		api.close();
		releaseWakeLock();
		super.onPause();
	}

	private void initView() {
		final ArrayList<View> aViews = new ArrayList<View>();
		LayoutInflater lf = LayoutInflater.from(this);
		vg = (ViewGroup) lf.inflate(R.layout.location_activity, null);
		ggaView = lf.inflate(R.layout.gga, null);
		gllView = lf.inflate(R.layout.gll, null);
		gsaView = lf.inflate(R.layout.gsa, null);
		gsvView = lf.inflate(R.layout.gsv, null);
		rmcView = lf.inflate(R.layout.rmc, null);
		vtgView = lf.inflate(R.layout.vtg, null);
		aViews.add(ggaView);
		aViews.add(gllView);
		aViews.add(gsaView);
		aViews.add(gsvView);
		aViews.add(rmcView);
		aViews.add(vtgView);
		mvp = (ViewPager) vg.findViewById(R.id.viewpager);
		ll = (LinearLayout) vg.findViewById(R.id.group);

		images = new ImageView[aViews.size()];
		for (int i = 0; i < images.length; i++) {
			iv = new ImageView(this);
			iv.setLayoutParams(new LayoutParams(20, 20));
			iv.setPadding(20, 0, 20, 0);
			if (i == 0) {
				iv.setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				iv.setBackgroundResource(R.drawable.page_indicator);
			}
			images[i] = iv;
			ll.addView(images[i]);
		}
		PagerAdapter pa = new PagerAdapter() {

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return aViews.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				// TODO Auto-generated method stub
				((ViewPager) container).removeView(aViews.get(position));
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				// TODO Auto-generated method stub
				return arg0 == arg1;
			}

			@Override
			public void finishUpdate(View arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public Object instantiateItem(View arg0, int arg1) {
				// TODO Auto-generated method stub
				((ViewPager) arg0).addView(aViews.get(arg1), 0);
				return aViews.get(arg1);
			}

			@Override
			public void restoreState(Parcelable arg0, ClassLoader arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public Parcelable saveState() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void startUpdate(View arg0) {
				// TODO Auto-generated method stub

			}
		};
		setContentView(vg);
		mvp.setAdapter(pa);
		mvp.setOnPageChangeListener(this);

		spinner1 = (Spinner) findViewById(R.id.spinner01);
		spinner2 = (Spinner) findViewById(R.id.spinner02);
		spinner3 = (Spinner) findViewById(R.id.spinner03);
		modeText = (TextView) findViewById(R.id.mode_title);

		ggaListView = (ListView) ggaView.findViewById(R.id.gga);
		gllListView = (ListView) gllView.findViewById(R.id.gll);
		gsaListView = (ListView) gsaView.findViewById(R.id.gsa);
		gsa2ListView = (ListView) gsaView.findViewById(R.id.gsa2);
		gsvListView = (ListView) gsvView.findViewById(R.id.gsv);
		gsv2ListView = (ListView) gsvView.findViewById(R.id.gsv2);
		rmcListView = (ListView) rmcView.findViewById(R.id.rmc);
		vtgListView = (ListView) vtgView.findViewById(R.id.vtg);
		listViews = new ListView[] { ggaListView, gllListView, gsaListView,
				gsvListView, rmcListView, vtgListView };
		
		satellite1 = (ListView) gsvView.findViewById(R.id.satellite1);
		satellite2 = (ListView) gsvView.findViewById(R.id.satellite2);
		
		prn1 = (ListView) gsaView.findViewById(R.id.prn1);
		prn2 = (ListView) gsaView.findViewById(R.id.prn2);
		
		list1.add(ggaList);
		list1.add(gllList);
		list1.add(gsaList);
		list1.add(gsvList);
		list1.add(rmcList);
		list1.add(vtgList);

		list2.add(null);
		list2.add(null);
		list2.add(gsa2List);
		list2.add(gsv2List);
		list2.add(null);
		list2.add(null);
	}

	private void initData() {
		myHandler = new MyHandler(application.getHandlerThread().getLooper());
		locationAdapter1 = new MyAdapter(this);
		locationAdapter2 = new MyAdapter(this);
		locationAdapter1.setList(list1);
		locationAdapter2.setList(list2);
		listViews[0].setAdapter(locationAdapter1);
		
		satelliteAdapter1 = new SatelliteAdapter(this);
		satelliteAdapter2 = new SatelliteAdapter(this);
		satellite1.setAdapter(satelliteAdapter1);
		satellite2.setAdapter(satelliteAdapter2);
		
		prnAdapter1 = new PrnAdapter(this); 
		prnAdapter2 = new PrnAdapter(this); 
		prn1.setAdapter(prnAdapter1);
		prn2.setAdapter(prnAdapter2);
		
		adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, START_CONTROL_COMMAND);
		adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MODE_SWITCH_COMMAND);
		adapter3 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				STATEMENT_FREQUENCY_COMMAND);

		spinner1.setAdapter(adapter1);
		spinner2.setAdapter(adapter2);
		spinner3.setAdapter(adapter3);

		spinner1.setOnItemSelectedListener(new Spinner1Listener());
		spinner2.setOnItemSelectedListener(new Spinner2Listener());
		spinner3.setOnItemSelectedListener(new Spinner3Listener());
		modeText.setText(MODE_TITLE[0]);
	}

	class Spinner1Listener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View view,
				int position, long arg3) {
			Log.i("whw", "Spinner1Listener");
			if (isStart1) {
				isStart1 = false;
				return;
			}
			myHandler.obtainMessage(1, command1[position]).sendToTarget();
			Log.i("xuwsxx", "position=" + position);
		}

		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	class Spinner2Listener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View view,
				int position, long arg3) {
			Log.i("xuwsxx", "Spinner2Listener");
			if (isStart2) {
				isStart2 = false;
				return;
			}
			if(position==0){
				gsaListView.setVisibility(View.VISIBLE);
				prn1.setVisibility(View.VISIBLE);
				gsvListView.setVisibility(View.VISIBLE);
				satellite1.setVisibility(View.VISIBLE);
				gsa2ListView.setVisibility(View.VISIBLE);
				prn2.setVisibility(View.VISIBLE);
				gsv2ListView.setVisibility(View.VISIBLE);
				satellite2.setVisibility(View.VISIBLE);
			}else if(position==1){
				gsaListView.setVisibility(View.VISIBLE);
				prn1.setVisibility(View.VISIBLE);
				gsvListView.setVisibility(View.VISIBLE);
				satellite1.setVisibility(View.VISIBLE);
				gsa2ListView.setVisibility(View.GONE);
				prn2.setVisibility(View.GONE);
				gsv2ListView.setVisibility(View.GONE);
				satellite2.setVisibility(View.GONE);
			}else if(position==2){
				gsaListView.setVisibility(View.GONE);
				prn1.setVisibility(View.GONE);
				gsvListView.setVisibility(View.GONE);
				satellite1.setVisibility(View.GONE);
				gsa2ListView.setVisibility(View.VISIBLE);
				prn2.setVisibility(View.VISIBLE);
				gsv2ListView.setVisibility(View.VISIBLE);
				satellite2.setVisibility(View.VISIBLE);
			}
			modeText.setText(MODE_TITLE[position]);
			myHandler.obtainMessage(2, command2[position]).sendToTarget();

		}

		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	class Spinner3Listener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View view,
				int position, long arg3) {
			Log.i("xuwsxx", "Spinner3Listener");
			if (isStart3) {
				isStart3 = false;
				return;
			}
			myHandler.obtainMessage(3, command3[position]).sendToTarget();
		}

		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	private class MyHandler extends Handler {

		public MyHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i("whw", "msg.what=" + msg.what);
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				api.open();
				break;
			case 1:
				api.receive((byte[]) msg.obj);
				break;
			case 2:
				api.receive((byte[]) msg.obj);
				break;
			case 3:
				api.receive((byte[]) msg.obj);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	int selectIndex = 0;

	@Override
	public void onPageSelected(int arg0) {
		selectIndex = arg0;
		for (int i = 0; i < listViews.length; i++) {
			if (i == arg0) {
				listViews[i].setAdapter(locationAdapter1);
			} else {
				listViews[i].setAdapter(null);
			}
		}

		if (selectIndex != 2 && selectIndex != 3) {
			gsa2ListView.setAdapter(null);
			gsv2ListView.setAdapter(null);
		}

		if (selectIndex == 2) {
			gsa2ListView.setAdapter(locationAdapter2);
			gsv2ListView.setAdapter(null);
		}

		if (selectIndex == 3) {
			gsa2ListView.setAdapter(null);
			gsv2ListView.setAdapter(locationAdapter2);
		}

		for (int i = 0; i < images.length; i++) {
			if (i == arg0) {
				images[i]
						.setBackgroundResource(R.drawable.page_indicator_focused);
			} else {
				images[i].setBackgroundResource(R.drawable.page_indicator);
			}
		}

	}

	private class MyAdapter extends BaseAdapter {
		private ArrayList<ArrayList<String>> list;
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public void setList(ArrayList<ArrayList<String>> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			}
			return list.get(selectIndex).size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item, null);
				holder = new ViewHolder();
				holder.key = (TextView) convertView.findViewById(R.id.key);
				holder.value = (TextView) convertView.findViewById(R.id.value);
				convertView.setTag(holder);// 绑定ViewHolder对象
			} else {
				holder = (ViewHolder) convertView.getTag();// 取出ViewHolder对象
			}
			holder.key.setText(keys[selectIndex][position]);
			holder.value.setText(list.get(selectIndex).get(position));

			return convertView;
		}

		public final class ViewHolder {
			public TextView key;
			public TextView value;
		}

	}
	
	private class SatelliteAdapter extends BaseAdapter {
		private List<Satellite> list;
		private LayoutInflater mInflater;

		public SatelliteAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public void setList(List<Satellite> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			}
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.satellite_item, null);
				holder = new ViewHolder();
				holder.SatelliteID = (TextView) convertView.findViewById(R.id.SatelliteID);
				holder.Elevation = (TextView) convertView.findViewById(R.id.Elevation);
				holder.Azimuth = (TextView) convertView.findViewById(R.id.Azimuth);
				holder.SNR = (TextView) convertView.findViewById(R.id.SNR);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.SatelliteID.setText(list.get(position).getSatelliteID());
			holder.Elevation.setText(list.get(position).getElevation());
			holder.Azimuth.setText(list.get(position).getAzimuth());
			holder.SNR.setText(list.get(position).getSNR());
			return convertView;
		}

		public final class ViewHolder {
			public TextView SatelliteID;
			public TextView Elevation;
			public TextView Azimuth;
			public TextView SNR;
		}

	}
	
	private class PrnAdapter extends BaseAdapter {
		private List<String> list;
		private LayoutInflater mInflater;

		public PrnAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public void setList(List<String> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			}
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.prn_item, null);
				holder = new ViewHolder();
				holder.prn = (TextView) convertView.findViewById(R.id.prn_key);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.prn.setText(list.get(position));
			return convertView;
		}

		public final class ViewHolder {
			public TextView prn;
		}

	}

}
