package com.authentication.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.authentication.asynctask.AsyncQRScanner;
import com.authentication.utils.DataUtils;
import com.szsicod.print.escpos.PrinterAPI;
import com.szsicod.print.io.InterfaceAPI;
import com.szsicod.print.io.SerialAPI;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android_serialport_api.SerialPortManager;

public class PrintActivity extends Activity implements OnClickListener{
	private String path = "/dev/ttyHSL1";
	private int baud_rate_int = 38400;
	private PrinterAPI mPrinter = new PrinterAPI();
	private int REQUEST_CODE_PICK_IMAGE = 1;
	private Runnable runnable;
	private EditText editTextText;
	private EditText editTextBarcode;
	private EditText editTextQRCode;
	private ImageView imageView;
	private Button buttoncConnect;
	private Button buttoncDisonnect;
	private Button buttoncPrintText;
	private Button buttoncPrintBarcode;
	private Button buttoncPrintQRCode;
	private Button buttoncPrintBitmap;
	private Button button_cut;
	
	private InnerThread thread;
	private boolean isRunning = false;
	private AsyncQRScanner asyncQRScanner;
	private MyApplication application;
	private static final int FUNCTION_SUCCESS = 0;
	private static final int FUNCTION_FAIL = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print);
		
		initView();
		
		application = (MyApplication) this.getApplicationContext();
		asyncQRScanner = new AsyncQRScanner(application.getHandlerThread().getLooper(), handler);
		startRead();
	}
	
	private void initView() {
		editTextText = (EditText) findViewById(R.id.edittext_text);
		editTextBarcode = (EditText) findViewById(R.id.edittext_barcode);
		editTextQRCode = (EditText) findViewById(R.id.edittext_qrcode);
		imageView = (ImageView) findViewById(R.id.imageview_bitmap);
		imageView.setOnClickListener(this);
		buttoncConnect = (Button) findViewById(R.id.button_connect);
		buttoncConnect.setOnClickListener(this);
		buttoncDisonnect = (Button) findViewById(R.id.button_close);
		buttoncDisonnect.setOnClickListener(this);
		buttoncPrintText = (Button) findViewById(R.id.button_print_text);
		buttoncPrintText.setOnClickListener(this);
		buttoncPrintBarcode = (Button) findViewById(R.id.button_print_barcode);
		buttoncPrintBarcode.setOnClickListener(this);
		buttoncPrintQRCode = (Button) findViewById(R.id.button_print_qrcode);
		buttoncPrintQRCode.setOnClickListener(this);
		buttoncPrintBitmap = (Button) findViewById(R.id.button_print_bitmap);
		buttoncPrintBitmap.setOnClickListener(this);
		button_cut = (Button) findViewById(R.id.button_cut);
		button_cut.setOnClickListener(this);
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FUNCTION_SUCCESS:
				Toast.makeText(PrintActivity.this, R.string.func_success,Toast.LENGTH_SHORT).show();
				break;
			case FUNCTION_FAIL:
				Toast.makeText(PrintActivity.this, R.string.func_failed,Toast.LENGTH_SHORT).show();
				break;
			case AsyncQRScanner.READ_CODE://读取二维码成功并解析
				byte[] code = (byte[]) msg.obj;
				editTextQRCode.setText("");
				if(code!=null){
					String QRcode = "";
					for (int i = 6; i < code.length-1; i++) {
						QRcode += (char)code[i]+"";
					}
					Log.d("jokey", "QRcode"+QRcode+"code"+DataUtils.toHexString(code));
					editTextQRCode.setText(QRcode);
					printQRCode();//打印二维码
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	protected void getImageFromAlbum() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");// 
		startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
	}
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		if (resultCode != 0 && requestCode == REQUEST_CODE_PICK_IMAGE) {
			runnable = new Runnable() {
				@Override
				public void run() {
					Uri uri = data.getData();
					// to do find the path of pic
					if (uri.getPath() != "") {
						try {
							Bitmap photoBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
							if (photoBmp != null) {
								if (PrinterAPI.SUCCESS == mPrinter.printRasterBitmap(photoBmp)) {
									handler.sendEmptyMessage(0);

								} else {
									handler.sendEmptyMessage(1);

								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
			new Thread(runnable).start();

		}
	}
	
	private void startRead(){
		if(thread==null){
			thread = new InnerThread();
			isRunning = true;
		}
		thread.start();
	}
	
	private void stopRead(){
		if(thread!=null){
			thread = null;
			isRunning = false;
		}
	}
	
	private class InnerThread extends Thread{
		@Override
		public void run() {
			while(isRunning){
				asyncQRScanner.readCode();
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private void printQRCode(){
		runnable = new Runnable() {
			public void run() {
				mPrinter.printQRCode(editTextQRCode.getText().toString(), 1, 12);
			}
		};
		new Thread(runnable).start();
	}
	@Override
	protected void onResume() {
		SerialPortManager.getInstance().openSerialPort();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		isRunning = false;
		stopRead();
		mPrinter.disconnect();
		SerialPortManager.getInstance().closeSerialPort();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_connect://连接设备
			InterfaceAPI io = new SerialAPI(new File(path),baud_rate_int);
			if (PrinterAPI.SUCCESS == mPrinter.connect(io)) {
				handler.sendEmptyMessage(0);
			} else {
				handler.sendEmptyMessage(1);
			}
			break;
		case R.id.button_close://关闭连接
			if (PrinterAPI.SUCCESS == mPrinter.disconnect()) {
				handler.sendEmptyMessage(0);
			} else {
				handler.sendEmptyMessage(1);
			}
			break;
		case R.id.button_print_text://打印文本
			runnable = new Runnable() {
				@Override
				public void run() {
					 try {
						mPrinter.printString(editTextText.getText().toString(), "GBK");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(runnable).start();
			break;
		case R.id.button_print_barcode://打印条码
			runnable = new Runnable() {
				@Override
				public void run() {
					mPrinter.printBarCode(69, 10,editTextBarcode.getText().toString());
				}
			};
			new Thread(runnable).start();
			break;
		case R.id.button_print_qrcode://打印二维码
			printQRCode();
			break;
		case R.id.button_print_bitmap://打印图片
			runnable = new Runnable() {
				@Override
				public void run() {
					try {
						imageView.setDrawingCacheEnabled(true);
						mPrinter.printRasterBitmap(imageView.getDrawingCache());
						imageView.setDrawingCacheEnabled(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(runnable).start();
			break;
		case R.id.button_cut://切纸
			runnable = new Runnable() {
				@Override
				public void run() {
					try {
						mPrinter.printString("", "GBK");
						mPrinter.cutPaper(66, 0);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(runnable).start();
			break;
		case R.id.imageview_bitmap://选图片
			getImageFromAlbum();
			break;
		}
	}
}
