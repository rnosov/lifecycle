package com.authentication.activity;

import java.lang.ref.WeakReference;

import com.authentication.activity.MySettingDialog.OnMySettingCallback;
import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android_serialport_api.UHFHXAPI;

public class HXUHFActivity extends BaseUHFActivity {
	private Button singleSearch;
	private Button mBtSetting;

	UHFHXAPI api;

	/**
	 * 用于集中处理显示等事件信息的静态类
	 * 
	 * @author chenshanjing
	 * 
	 */
	class StartHander extends Handler {
		WeakReference<Activity> mActivityRef;

		StartHander(Activity activity) {
			mActivityRef = new WeakReference<Activity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			Activity activity = mActivityRef.get();
			if (activity == null) {
				return;
			}

			switch (msg.what) {
			case MSG_SHOW_EPC_INFO:
				ShowEPC((String) msg.obj);
				break;

			case MSG_DISMISS_CONNECT_WAIT_SHOW:
				prgDlg.dismiss();
				if ((Boolean) msg.obj) {
					Toast.makeText(activity,activity.getText(R.string.info_connect_success),Toast.LENGTH_SHORT).show();
//					byte[] data = api.setRegion(0x52).data;
//					byte[] data  = api.getRegion().data;
//					Log.d("jokey", "data-->"+DataUtils.toHexString(data));
					setting.setEnabled(true);
					buttonInv.setClickable(true);
				} else {
					Toast.makeText(activity,activity.getText(R.string.info_connect_fail),Toast.LENGTH_SHORT).show();
				}
				break;
			case INVENTORY_OVER:
				ToastUtil.showToast(HXUHFActivity.this, R.string.inventory_over);
				break;

			}
		}
	};

	private Handler hMsg = new StartHander(this);

	private Handler mhandler;

	private int times = 5000;// 默认超时5秒
	private int code = 1;// 默认读取epc区域
	private int sa = 0;// 默认偏移从0开始
	private int dl = 5;// 默认数据长度5
	private String pwd = "00000000";// 默认访问密码00000000

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_uhf);

		mhandler = new Handler();

		singleSearch = (Button) findViewById(R.id.bt_singleSearch);
		singleSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						api.startAutoRead2C(times, code, pwd, sa, dl,new UHFHXAPI.SearchAndRead() {
							@Override
							public void timeout() {
								mhandler.post(new Runnable() {
									@Override
									public void run() {
										ToastUtil.showToast(getApplicationContext(),"超时");
									}
								});
							}
							@Override
							public void returnData(final byte[] data) {
								mhandler.post(new Runnable() {
									@Override
									public void run() {
										ToastUtil.showToast(getApplicationContext(),"data:"+ DataUtils.toHexString(data));
									}
								});
							}
							@Override
							public void readFail() {
								mhandler.post(new Runnable() {
									@Override
									public void run() {
										ToastUtil.showToast(getApplicationContext(),"读取失败");
									}
								});
							}
						});
					}
				}).start();
			}
		});

		mBtSetting = (Button) findViewById(R.id.bt_setting);
		mBtSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MySettingDialog dialog = new MySettingDialog(HXUHFActivity.this);
				dialog.show();
				dialog.setOnMySettingCallback(new OnMySettingCallback() {

					@Override
					public void onSetting(int times, int code, String pwd,int sa, int dl) {
						HXUHFActivity.this.times = times;
						HXUHFActivity.this.code = code;
						HXUHFActivity.this.pwd = pwd;
						HXUHFActivity.this.sa = sa;
						HXUHFActivity.this.dl = dl;
					}
				});
			}
		});

		api = new UHFHXAPI();
		txtCount = (TextView) findViewById(R.id.txtCount);
		txtTimes = (TextView) findViewById(R.id.txtTimes);
		setting = (Button) findViewById(R.id.setting_params);
		setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UHFDialogFragment dialog = new UHFDialogFragment();
				dialog.show(getFragmentManager(), "corewise");
			}
		});
		buttonConnect = (ToggleButton) findViewById(R.id.togBtn_open);
		buttonInv = (ToggleButton) findViewById(R.id.togBtn_inv);
		final FragmentManager fragmentManager = getFragmentManager();
		objFragment = (TaglistFragment) fragmentManager.findFragmentById(R.id.fragment_taglist);

		buttonConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						buttonConnect.setClickable(false);
						if (isChecked) {
							if (prgDlg != null) {
								prgDlg.show();
							} else {
								prgDlg = ProgressDialog.show(HXUHFActivity.this, "连接设备","正在连接设备，请稍后...", true, false);
							}
							buttonInv.setClickable(true);
							new Thread() {
								@Override
								public void run() {
									Message closemsg = new Message();
									closemsg.obj = api.open();
									closemsg.what = MSG_DISMISS_CONNECT_WAIT_SHOW;
									hMsg.sendMessage(closemsg);
								}
							}.start();
						} else {
							if (!isOnPause) {
								api.close();
								setting.setEnabled(false);
							}
							buttonInv.setClickable(false);
						}

						buttonConnect.setClickable(true);
					}
				});

		buttonInv.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				buttonInv.setClickable(false);
				if (isChecked) {
					isStop = false;
					Inv();
					setting.setEnabled(false);
				} else {
					isStop = true;
					setting.setEnabled(true);
				}

				buttonInv.setClickable(true);
			}
		});
	}

	/**
	 * 显示搜索得到的标签信息
	 * 
	 * @param activity
	 * @param flagID
	 */
	public static void ShowEPC(String flagID) {
		if (mediaPlayer == null) {
			return;
		}
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.seekTo(0);
		} else {
			mediaPlayer.start();
		}
		if (!tagInfoList.contains(flagID)) {
			number.put(flagID, 1);
			tagCount++;
			tagInfoList.add(flagID);
			objFragment.addItem(flagID);

			try {
				txtCount.setText(String.format("%d", tagCount));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			int num = number.get(flagID);
			number.put(flagID, ++num);
			Log.i("whw", "flagID=" + flagID + "   num=" + num);
		}
		objFragment.myadapter.notifyDataSetChanged();
		tagTimes++;
		try {
			txtTimes.setText(String.format("%d", tagTimes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启盘点操作
	 */
	public void Inv() {
		pool.execute(task);
		tagInfoList.clear();
		tagCount = 0;
		tagTimes = 0;
		objFragment.clearItem();

		try {
			txtCount.setText(String.format("%d", tagCount));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			txtTimes.setText(String.format("%d", tagTimes));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isStop;
	private Runnable task = new Runnable() {

		@Override
		public void run() {
			api.startAutoRead2A(0x22, new byte[] { 0x00, 0x01 },new UHFHXAPI.AutoRead() {
				@Override
				public void timeout() {
					Log.i("zzd", "timeout");
				}
				@Override
				public void start() {
					Log.i("zzd", "start");
				}
				@Override
				public void processing(byte[] data) {
					String epc = DataUtils.toHexString(data).substring(4);
					hMsg.obtainMessage(MSG_SHOW_EPC_INFO, epc).sendToTarget();
					Log.i("zzd", "data=" + epc);
				}
				@Override
				public void end() {
					Log.i("whw", "end");
					Log.i("whw", "isStop=" + isStop);
					if (!isStop) {
						pool.execute(task);
					} else {
						hMsg.sendEmptyMessage(INVENTORY_OVER);
					}
				}
			});
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		isOnPause = false;
	}

	private boolean isOnPause;

	@Override
	protected void onPause() {
		isOnPause = true;
		isStop = true;
		if (buttonInv.isChecked()) {
			buttonInv.setChecked(false);
			buttonInv.setClickable(false);
			api.close();
		}
		if (buttonConnect.isChecked()) {
			buttonConnect.setChecked(false);
		}
		super.onPause();
	}
}