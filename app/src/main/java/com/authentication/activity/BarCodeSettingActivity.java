package com.authentication.activity;

import com.authentication.asynctask.AsyncBarCode;
import com.authentication.asynctask.AsyncBarCode.OnSetParameterListener;
import com.authentication.asynctask.AsyncBarCode.OnRestoreListener;
import com.authentication.utils.ToastUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android_serialport_api.BarCodeAPI;

public class BarCodeSettingActivity extends Activity implements OnClickListener ,OnCheckedChangeListener{

	private MyApplication application;
	protected HandlerThread handlerThread;
	private Button restore;
	private Button setHost;
	private Button disableAllSymbologies;
	

/********************1D Symbologies**********************/
	private RadioGroup upc_a;
	private RadioGroup upc_e;
	private RadioGroup upc_e1;
	private RadioGroup ean_8_jan_8;
	private RadioGroup ean_13_jan_13;
	private RadioGroup bookland_ean;
	private RadioGroup bookland_isbn_format;
	private RadioGroup ucc_coupon_extended_code;
	private RadioGroup issn_ean;
	private RadioGroup code_128;
	private RadioGroup gs1_128;
	private RadioGroup isbt_128;
	private RadioGroup code_39;
	private RadioGroup trioptic_code_39;
	private RadioGroup code_93;
	private RadioGroup code_11;
	private RadioGroup itf;
	private RadioGroup dtf;
	private RadioGroup codabar;
	private RadioGroup msi;
	private RadioGroup ctf;
	private RadioGroup mtf;
	private RadioGroup ktf;
	private RadioGroup inverse_1d;
	
/********************2D Symbologies**********************/
	
	private RadioGroup pdf417;
	private RadioGroup microPdf417;
	private RadioGroup code_128_emulation;
	private RadioGroup dataMatrix;
	private RadioGroup data_matrix_inverse;
	private RadioGroup decode_mirror_images;
	private RadioGroup maxicode;
	private RadioGroup qrCode;
	private RadioGroup qr_inverse;
	private RadioGroup microQR;
	private RadioGroup aztec;
	private RadioGroup aztec_inverse;
	private RadioGroup hanxin;
	
	/********************Postal Codes**********************/
	private RadioGroup us_postnet;
	private RadioGroup us_planet;
	private RadioGroup uk_postal;
	private RadioGroup japan_postal;
	private RadioGroup australia_post;
	private RadioGroup netherlands_kix_code;
	private RadioGroup usps_4cb_one_code_intelligent_mail;
	private RadioGroup upu_fics_postal;
	
	private ProgressDialog progressDialog;

	private AsyncBarCode asyncBarCode;
	
	private SharedPreferences preferences;

	MediaPlayer mediaPlayer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_setting_activity);
		initView();
		initData();
	}


	private void initView() {
		restore = (Button) findViewById(R.id.restore);
		restore.setOnClickListener(this);
		setHost = (Button) findViewById(R.id.set_host);
		setHost.setOnClickListener(this);
		disableAllSymbologies = (Button) findViewById(R.id.disable_all_symbologies);
		disableAllSymbologies.setOnClickListener(this);
		
		/********************1D Symbologies**********************/
		upc_a = (RadioGroup) findViewById(R.id.upc_a);
		upc_a.setOnCheckedChangeListener(this);
		upc_e = (RadioGroup) findViewById(R.id.upc_e);
		upc_e.setOnCheckedChangeListener(this);
		upc_e1 = (RadioGroup) findViewById(R.id.upc_e1);
		upc_e1.setOnCheckedChangeListener(this);
		ean_8_jan_8 = (RadioGroup) findViewById(R.id.ean_8_jan_8);
		ean_8_jan_8.setOnCheckedChangeListener(this);
		ean_13_jan_13 = (RadioGroup) findViewById(R.id.ean_13_jan_13);
		ean_13_jan_13.setOnCheckedChangeListener(this);
		bookland_ean = (RadioGroup) findViewById(R.id.bookland_ean);
		bookland_ean.setOnCheckedChangeListener(this);
		bookland_isbn_format = (RadioGroup) findViewById(R.id.bookland_isbn_format);
		bookland_isbn_format.setOnCheckedChangeListener(this);
		ucc_coupon_extended_code = (RadioGroup) findViewById(R.id.ucc_coupon_extended_code);
		ucc_coupon_extended_code.setOnCheckedChangeListener(this);
		issn_ean = (RadioGroup) findViewById(R.id.issn_ean);
		issn_ean.setOnCheckedChangeListener(this);
		code_128 = (RadioGroup) findViewById(R.id.code_128);
		code_128.setOnCheckedChangeListener(this);
		gs1_128 = (RadioGroup) findViewById(R.id.gs1_128);
		gs1_128.setOnCheckedChangeListener(this);
		isbt_128 = (RadioGroup) findViewById(R.id.isbt_128);
		isbt_128.setOnCheckedChangeListener(this);
		code_39 = (RadioGroup) findViewById(R.id.code_39);
		code_39.setOnCheckedChangeListener(this);
		trioptic_code_39 = (RadioGroup) findViewById(R.id.trioptic_code_39);
		trioptic_code_39.setOnCheckedChangeListener(this);
		code_93 = (RadioGroup) findViewById(R.id.code_93);
		code_93.setOnCheckedChangeListener(this);
		code_11 = (RadioGroup) findViewById(R.id.code_11);
		code_11.setOnCheckedChangeListener(this);
		itf = (RadioGroup) findViewById(R.id.itf);
		itf.setOnCheckedChangeListener(this);
		dtf = (RadioGroup) findViewById(R.id.dtf);
		dtf.setOnCheckedChangeListener(this);
		codabar = (RadioGroup) findViewById(R.id.codabar);
		codabar.setOnCheckedChangeListener(this);
		msi = (RadioGroup) findViewById(R.id.msi);
		msi.setOnCheckedChangeListener(this);
		ctf = (RadioGroup) findViewById(R.id.ctf);
		ctf.setOnCheckedChangeListener(this);
		mtf = (RadioGroup) findViewById(R.id.mtf);
		mtf.setOnCheckedChangeListener(this);
		ktf = (RadioGroup) findViewById(R.id.ktf);
		ktf.setOnCheckedChangeListener(this);
		inverse_1d = (RadioGroup) findViewById(R.id.inverse_1d);
		inverse_1d.setOnCheckedChangeListener(this);
		
		/********************2D Symbologies**********************/
		pdf417 = (RadioGroup) findViewById(R.id.pdf417);
		pdf417.setOnCheckedChangeListener(this);
		
		microPdf417 = (RadioGroup) findViewById(R.id.micro_pdf417);
		microPdf417.setOnCheckedChangeListener(this);
		
		code_128_emulation = (RadioGroup) findViewById(R.id.code_128_emulation);
		code_128_emulation.setOnCheckedChangeListener(this);
		
		dataMatrix = (RadioGroup) findViewById(R.id.data_matrix);
		dataMatrix.setOnCheckedChangeListener(this);
		
		data_matrix_inverse = (RadioGroup) findViewById(R.id.data_matrix_inverse);
		data_matrix_inverse.setOnCheckedChangeListener(this);
		
		decode_mirror_images = (RadioGroup) findViewById(R.id.decode_mirror_images);
		decode_mirror_images.setOnCheckedChangeListener(this);
		
		maxicode = (RadioGroup) findViewById(R.id.maxicode);
		maxicode.setOnCheckedChangeListener(this);
		
		qrCode = (RadioGroup) findViewById(R.id.qrcode);
		qrCode.setOnCheckedChangeListener(this);
		
		qr_inverse = (RadioGroup) findViewById(R.id.qr_inverse);
		qr_inverse.setOnCheckedChangeListener(this);
		
		microQR = (RadioGroup) findViewById(R.id.micro_qr);
		microQR.setOnCheckedChangeListener(this);
		
		aztec = (RadioGroup) findViewById(R.id.aztec);
		aztec.setOnCheckedChangeListener(this);
		
		aztec_inverse = (RadioGroup) findViewById(R.id.aztec_inverse);
		aztec_inverse.setOnCheckedChangeListener(this);
		
		hanxin = (RadioGroup) findViewById(R.id.hanxin);
		hanxin.setOnCheckedChangeListener(this);
		
		/********************Postal Codes**********************/
		us_postnet = (RadioGroup) findViewById(R.id.us_postnet);
		us_postnet.setOnCheckedChangeListener(this);
		
		us_planet = (RadioGroup) findViewById(R.id.us_planet);
		us_planet.setOnCheckedChangeListener(this);
		
		uk_postal = (RadioGroup) findViewById(R.id.uk_postal);
		uk_postal.setOnCheckedChangeListener(this);
		
		japan_postal = (RadioGroup) findViewById(R.id.japan_postal);
		japan_postal.setOnCheckedChangeListener(this);
		
		australia_post = (RadioGroup) findViewById(R.id.australia_post);
		australia_post.setOnCheckedChangeListener(this);
		
		netherlands_kix_code = (RadioGroup) findViewById(R.id.netherlands_kix_code);
		netherlands_kix_code.setOnCheckedChangeListener(this);
		
		usps_4cb_one_code_intelligent_mail = (RadioGroup) findViewById(R.id.usps_4cb_one_code_intelligent_mail);
		usps_4cb_one_code_intelligent_mail.setOnCheckedChangeListener(this);
		
		upu_fics_postal = (RadioGroup) findViewById(R.id.upu_fics_postal);
		upu_fics_postal.setOnCheckedChangeListener(this);


	}

	private void initData() {
		application = (MyApplication) getApplicationContext();
		handlerThread = application.getHandlerThread();
		mediaPlayer = MediaPlayer.create(this, R.raw.barcode);
		preferences = getSharedPreferences("init", MODE_PRIVATE);
		asyncBarCode = new AsyncBarCode(application.getHandlerThread().getLooper());
		
		asyncBarCode.setOnRestoreListener(new OnRestoreListener() {
			
			@Override
			public void onRestoreSuccess() {
				ToastUtil.showToast(BarCodeSettingActivity.this, R.string.set_success);
				Editor editor = preferences.edit();
				editor.putBoolean("isInit", false);
				editor.commit();
			}
			
			@Override
			public void onRestoreFail() {
				ToastUtil.showToast(BarCodeSettingActivity.this, R.string.set_fail);
				
			}
		});
		
		asyncBarCode.setOnSetParameterListener(new OnSetParameterListener() {
			
			@Override
			public void OnSetParameterSuccess() {
				ToastUtil.showToast(BarCodeSettingActivity.this, R.string.set_success);
				
			}
			
			@Override
			public void OnSetParameterFail() {
				ToastUtil.showToast(BarCodeSettingActivity.this, R.string.set_fail);
				
			}
		});
		
		

	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.restore:
			asyncBarCode.restore();
			break;
		case R.id.set_host:
			asyncBarCode.setHost();
			break;
		case R.id.disable_all_symbologies:
			asyncBarCode.disableAllSymbologies();
			break;
		default:
			break;
		}

	}
	
	



	private void showProgressDialog(String message) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage(message);
		if (!progressDialog.isShowing()) {
			progressDialog.show();
		}
	}

	private void cancleProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int groupId = group.getId();
		switch (groupId) {
		case R.id.upc_a:
			if(R.id.enable_upc_a == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.UPC_A_ENABLE);
				Log.i("whw", "UPC_A_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.UPC_A_DISABLE);
				Log.i("whw", "UPC_A_DISABLE");
			}
			break;
		case R.id.upc_e:
			if(R.id.enable_upc_e == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.UPC_E_ENABLE);
				Log.i("whw", "UPC_E_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.UPC_E_DISABLE);
				Log.i("whw", "UPC_E_DISABLE");
			}
			break;
		case R.id.upc_e1:
			if(R.id.enable_upc_e1 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.UPC_E1_ENABLE);
				Log.i("whw", "UPC_E1_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.UPC_E1_DISABLE);
				Log.i("whw", "UPC_E1_DISABLE");
			}
			break;
		case R.id.ean_8_jan_8:
			if(R.id.enable_ean_8_jan_8 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.EAN8_JAN8_ENABLE);
				Log.i("whw", "EAN8_JAN8_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.EAN8_JAN8_DISABLE);
				Log.i("whw", "EAN8_JAN8_DISABLE");
			}
			break;
		case R.id.ean_13_jan_13:
			if(R.id.enable_ean_13_jan_13 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.EAN13_JAN13_ENABLE);
				Log.i("whw", "EAN13_JAN13_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.EAN13_JAN13_DISABLE);
				Log.i("whw", "EAN13_JAN13_DISABLE");
			}
			break;
		case R.id.bookland_ean:
			if(R.id.enable_bookland_ean == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.BOOKLAND_EAN_ENABLE);
				Log.i("whw", "BOOKLAND_EAN_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.BOOKLAND_EAN_DISABLE);
				Log.i("whw", "BOOKLAND_EAN_DISABLE");
			}
			break;
		case R.id.bookland_isbn_format:
			if(R.id.enable_bookland_isbn_10 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.BOOKLAND_ISBN_10);
				Log.i("whw", "BOOKLAND_ISBN_10");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.BOOKLAND_ISBN_13);
				Log.i("whw", "BOOKLAND_ISBN_13");
			}
			break;
		case R.id.ucc_coupon_extended_code:
			if(R.id.enable_ucc_coupon_extended_code == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.UCC_COUPON_EXTENDED_CODE_ENABLE);
				Log.i("whw", "UCC_COUPON_EXTENDED_CODE_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.UCC_COUPON_EXTENDED_CODE_DISABLE);
				Log.i("whw", "UCC_COUPON_EXTENDED_CODE_DISABLE");
			}
			break;
		case R.id.issn_ean:
			if(R.id.enable_issn_ean == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.ISSN_EAN_ENABLE);
				Log.i("whw", "ISSN_EAN_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.ISSN_EAN_DISABLE);
				Log.i("whw", "ISSN_EAN_DISABLE");
			}
			break;
		case R.id.code_128:
			if(R.id.enable_code_128 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CODE_128_ENABLE);
				Log.i("whw", "CODE_128_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CODE_128_DISABLE);
				Log.i("whw", "CODE_128_DISABLE");
			}
			break;
		case R.id.gs1_128:
			if(R.id.enable_gs1_128 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.GS1_128_ENABLE);
				Log.i("whw", "GS1_128_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.GS1_128_DISABLE);
				Log.i("whw", "GS1_128_DISABLE");
			}
			break;
		case R.id.isbt_128:
			if(R.id.enable_isbt_128 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.ISBT_128_ENABLE);
				Log.i("whw", "ISBT_128_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.ISBT_128_DISABLE);
				Log.i("whw", "ISBT_128_DISABLE");
			}
			break;
		case R.id.code_39:
			if(R.id.enable_code_39 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CODE_39_ENABLE);
				Log.i("whw", "CODE_39_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CODE_39_DISABLE);
				Log.i("whw", "CODE_39_DISABLE");
			}
			break;
		case R.id.trioptic_code_39:
			if(R.id.enable_trioptic_code_39 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.TRIOPTIC_CODE_39_ENABLE);
				Log.i("whw", "TRIOPTIC_CODE_39_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.TRIOPTIC_CODE_39_DISABLE);
				Log.i("whw", "TRIOPTIC_CODE_39_DISABLE");
			}
			break;
		case R.id.code_93:
			if(R.id.enable_code_93 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CODE_93_ENABLE);
				Log.i("whw", "CODE_93_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CODE_93_DISABLE);
				Log.i("whw", "CODE_93_DISABLE");
			}
			break;
		case R.id.code_11:
			if(R.id.enable_code_11 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CODE_11_ENABLE);
				Log.i("whw", "CODE_11_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CODE_11_DISABLE);
				Log.i("whw", "CODE_11_DISABLE");
			}
			break;
		case R.id.itf:
			if(R.id.enable_itf == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.ITF_ENABLE);
				Log.i("whw", "ITF_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.ITF_DISABLE);
				Log.i("whw", "ITF_DISABLE");
			}
			break;
		case R.id.dtf:
			if(R.id.enable_dtf == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.DTF_ENABLE);
				Log.i("whw", "DTF_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.DTF_DISABLE);
				Log.i("whw", "DTF_DISABLE");
			}
			break;
		case R.id.codabar:
			if(R.id.enable_codabar == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CODABAR_ENABLE);
				Log.i("whw", "CODABAR_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CODABAR_DISABLE);
				Log.i("whw", "CODABAR_DISABLE");
			}
			break;
		case R.id.msi:
			if(R.id.enable_msi == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.MSI_ENABLE);
				Log.i("whw", "MSI_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.MSI_DISABLE);
				Log.i("whw", "MSI_DISABLE");
			}
			break;
		case R.id.ctf:
			if(R.id.enable_ctf == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CTF_ENABLE);
				Log.i("whw", "CTF_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CTF_DISABLE);
				Log.i("whw", "CTF_DISABLE");
			}
			break;
		case R.id.mtf:
			if(R.id.enable_mtf == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.MTF_ENABLE);
				Log.i("whw", "MTF_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.MTF_DISABLE);
				Log.i("whw", "MTF_DISABLE");
			}
			break;
		case R.id.ktf:
			if(R.id.enable_ktf == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.KTF_ENABLE);
				Log.i("whw", "KTF_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.KTF_DISABLE);
				Log.i("whw", "KTF_DISABLE");
			}
			break;
		case R.id.inverse_1d:
			if(R.id.inverse_1d_regular_only == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.INVERSE_1D_REGULAR_ONLY);
				Log.i("whw", "INVERSE_1D_REGULAR_ONLY");
			}else if(R.id.inverse_1d_inverse_only == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.INVERSE_1D_INVERSE_ONLY);
				Log.i("whw", "INVERSE_1D_INVERSE_ONLY");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.INVERSE_1D_INVERSE_AUTODETECT);
				Log.i("whw", "INVERSE_1D_INVERSE_AUTODETECT");
			}
			break;
		case R.id.pdf417:
			if(R.id.enable_pdf417 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.PDF417_ENABLE);
				Log.i("whw", "setEnablePDF417(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.PDF417_DISABLE);
				Log.i("whw", "setEnablePDF417(false)");
			}
			break;
		case R.id.micro_pdf417:
			if(R.id.enable_micro_pdf417 == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.MICRO_PDF417_ENABLE);
				Log.i("whw", "setEnableMicroPDF417(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.MICRO_PDF417_DISABLE);
				Log.i("whw", "setEnableMicroPDF417(false)");
			}
			break;
		case R.id.code_128_emulation:
			if(R.id.enable_code_128_emulation == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.CODE_128_EMULATION_ENABLE);
				Log.i("whw", "CODE_128_EMULATION_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.CODE_128_EMULATION_DISABLE);
				Log.i("whw", "CODE_128_EMULATION_DISABLE");
			}
			break;
		case R.id.data_matrix:
			if(R.id.enable_data_matrix == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_ENABLE);
				Log.i("whw", "setEnableDataMatrix(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_DISABLE);
				Log.i("whw", "setEnableDataMatrix(false)");
			}
			break;
		case R.id.data_matrix_inverse:
			if(R.id.data_matrix_regular_only == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_REGULAR_ONLY);
				Log.i("whw", "DATA_MATRIX_REGULAR_ONLY");
			}else if(R.id.data_matrix_inverse_only == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_INVERSE_ONLY);
				Log.i("whw", "DATA_MATRIX_INVERSE_ONLY");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.DATA_MATRIX_INVERSE_AUTODETECT);
				Log.i("whw", "DATA_MATRIX_INVERSE_AUTODETECT");
			}
			break;
		case R.id.decode_mirror_images:
			if(R.id.decode_mirror_images_never == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.DECODE_MIRROR_IMAGES_NEVER);
				Log.i("whw", "DECODE_MIRROR_IMAGES_NEVER");
			}else if(R.id.decode_mirror_images_always == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.DECODE_MIRROR_IMAGES_ALWAYS);
				Log.i("whw", "DECODE_MIRROR_IMAGES_ALWAYS");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.DECODE_MIRROR_IMAGES_AUTO);
				Log.i("whw", "DECODE_MIRROR_IMAGES_AUTO");
			}
            break;
		case R.id.maxicode:
			if(R.id.enable_maxicode == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.MAXICODE_ENABLE);
				Log.i("whw", "setEnableMaxicode(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.MAXICODE_DISABLE);
				Log.i("whw", "setEnableMaxicode(false)");
			}
			break;
		case R.id.qrcode:
			if(R.id.enable_qr_code == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.QR_CODE_ENABLE);
				Log.i("whw", "setEnableQRCode(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.QR_CODE_DISABLE);
				Log.i("whw", "setEnableQRCode(false)");
			}
			break;
		case R.id.qr_inverse:
			if(R.id.qr_inverse_regular == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.QR_INVERSE_REGULAR);
				Log.i("whw", "QR_INVERSE_REGULAR");
			}else if(R.id.qr_inverse_only == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.QR_INVERSE_ONLY);
				Log.i("whw", "QR_INVERSE_ONLY");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.QR_INVERSE_AUTODETECT);
				Log.i("whw", "QR_INVERSE_AUTODETECT");
			}
			break;
		case R.id.micro_qr:
			if(R.id.enable_micro_qr == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.MICRO_QR_ENABLE);
				Log.i("whw", "setEnableMicroQR(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.MICRO_QR_DISABLE);
				Log.i("whw", "setEnableMicroQR(false)");
			}
			break;
		case R.id.aztec:
			if(R.id.enable_aztec == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_ENABLE);
				Log.i("whw", "setEnableAztec(true)");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_DISABLE);
				Log.i("whw", "setEnableAztec(false)");
			}
			break;
		case R.id.aztec_inverse:
			if(R.id.aztec_inverse_regular == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_REGULAR_ONLY);
				Log.i("whw", "AZTEC_REGULAR_ONLY");
			}else if(R.id.aztec_inverse_only == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_INVERSE_ONLY);
				Log.i("whw", "AZTEC_INVERSE_ONLY");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.AZTEC_INVERSE_AUTODETECT);
				Log.i("whw", "AZTEC_INVERSE_AUTODETECT");
			}
			break;
		case R.id.hanxin:
			if(R.id.enable_hanxin == checkedId){
				asyncBarCode.getApi().enbaleHanxin();
				Log.i("whw", "setEnablehanxin(true)");
			}else{
				asyncBarCode.getApi().disableHanxin();
				Log.i("whw", "setEnablehanxin(false)");
			}
			break;
		case R.id.us_postnet:
			if(R.id.enable_us_postnet == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.US_POSTNET_ENABLE);
				Log.i("whw", "US_POSTNET_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.US_POSTNET_DISABLE);
				Log.i("whw", "US_POSTNET_DISABLE");
			}
			break;
		case R.id.us_planet:
			if(R.id.enable_us_planet == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.US_PLANET_ENABLE);
				Log.i("whw", "US_PLANET_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.US_PLANET_DISABLE);
				Log.i("whw", "US_PLANET_DISABLE");
			}
			break;
		case R.id.uk_postal:
			if(R.id.enable_uk_postal == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.UK_POSTAL_ENABLE);
				Log.i("whw", "UK_POSTAL_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.UK_POSTAL_DISABLE);
				Log.i("whw", "UK_POSTAL_DISABLE");
			}
			break;
		case R.id.japan_postal:
			if(R.id.enable_japan_postal == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.JAPAN_POSTAL_ENABLE);
				Log.i("whw", "JAPAN_POSTAL_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.JAPAN_POSTAL_DISABLE);
				Log.i("whw", "JAPAN_POSTAL_DISABLE");
			}
			break;
		case R.id.australia_post:
			if(R.id.enable_australia_post == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.AUSTRALIA_POST_ENABLE);
				Log.i("whw", "AUSTRALIA_POST_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.AUSTRALIA_POST_DISABLE);
				Log.i("whw", "AUSTRALIA_POST_DISABLE");
			}
			break;
		case R.id.netherlands_kix_code:
			if(R.id.enable_netherlands_kix_code == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.NETHERLANDS_KIX_CODE_ENABLE);
				Log.i("whw", "NETHERLANDS_KIX_CODE_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.NETHERLANDS_KIX_CODE_DISABLE);
				Log.i("whw", "NETHERLANDS_KIX_CODE_DISABLE");
			}
			break;
		case R.id.usps_4cb_one_code_intelligent_mail:
			if(R.id.enable_usps_4cb_one_code_intelligent_mail == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.USPS_4CB_ONE_CODE_INTELLIGENT_MAIL_ENABLE);
				Log.i("whw", "USPS_4CB_ONE_CODE_INTELLIGENT_MAIL_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.USPS_4CB_ONE_CODE_INTELLIGENT_MAIL_DISABLE);
				Log.i("whw", "USPS_4CB_ONE_CODE_INTELLIGENT_MAIL_DISABLE");
			}
			break;
		case R.id.upu_fics_postal:
			if(R.id.enable_upu_fics_postal == checkedId){
				asyncBarCode.setParameter(BarCodeAPI.UPU_FICS_POSTAL_ENABLE);
				Log.i("whw", "UPU_FICS_POSTAL_ENABLE");
			}else{
				asyncBarCode.setParameter(BarCodeAPI.UPU_FICS_POSTAL_DISABLE);
				Log.i("whw", "UPU_FICS_POSTAL_DISABLE");
			}
			break;
		default:
			break;
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


}
