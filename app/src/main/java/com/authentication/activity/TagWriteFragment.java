package com.authentication.activity;

import com.authentication.utils.DataUtils;
import com.authentication.utils.ToastUtil;
import com.google.common.primitives.Bytes;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android_serialport_api.UHFHXAPI.Response;

public class TagWriteFragment extends Fragment {
	private Spinner spinnerArea;
	private TextView txtEpc;
	private EditText editInput;
	private MyunmberinputSpinner unmpOffset;
	private MyunmberinputSpinner unmpLength;
	private EditText editAccesspwd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tag_write_layout, container,
				false);
		spinnerArea = (Spinner) rootView.findViewById(R.id.spinnerArea);
		String[] areas = new String[] { "EPC", "USER" };

		ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(
				rootView.getContext(), R.layout.simple_list_item, areas);
		areaAdapter.setDropDownViewResource(R.layout.simple_list_item);
		spinnerArea.setAdapter(areaAdapter);

		final Button buttonWrite = (Button) rootView
				.findViewById(R.id.buttonWrite);
		buttonWrite.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonWrite.setClickable(false);
				write();
				buttonWrite.setClickable(true);
			}
		});

		txtEpc = (TextView) rootView.findViewById(R.id.txtWriteEpc);
		editInput = (EditText) rootView.findViewById(R.id.editInputInfo);
		unmpOffset = (MyunmberinputSpinner) rootView
				.findViewById(R.id.myunmberinputSpinner_offset);
		unmpLength = (MyunmberinputSpinner) rootView
				.findViewById(R.id.myunmberinputSpinner_length);
		editAccesspwd = (EditText) rootView.findViewById(R.id.editAccesspwd);

		return rootView;
	}

	public void write() {
		String ap = editAccesspwd.getText().toString();
		short epcLength = (short) (txtEpc.getText().toString().length() / 2);
		String epc = txtEpc.getText().toString();
		byte mb = (byte) spinnerArea.getSelectedItemPosition();
		switch (mb) {
		case 0:
			mb++;
			break;
		case 1:
			mb += 2;
			break;
		default:
			break;
		}
		short sa = Short.parseShort(unmpOffset.getSelectedItem().toString());
		short dl = Short.parseShort(unmpLength.getSelectedItem().toString());
		String writeData = editInput.getText().toString();
		if (!TextUtils.isEmpty(writeData) && writeData.length() / 4 == dl) {
			byte[] arguments = Bytes.concat(DataUtils.hexStringTobyte(ap),
					DataUtils.short2byte(epcLength),
					DataUtils.hexStringTobyte(epc), new byte[] { mb },
					DataUtils.short2byte(sa), DataUtils.short2byte(dl),
					DataUtils.hexStringTobyte(writeData));
			String data = writeTag(arguments);
			if (!TextUtils.isEmpty(writeData) && data.equals("00")) {
				ToastUtil.showToast(getActivity(), "写入成功！");
			} else {
				ToastUtil.showToast(getActivity(), "写入失败！");
			}
		}else{
			ToastUtil.showToast(getActivity(), "写入数据的长度不对！");
		}
	}

	public String writeTag(byte[] args) {
		Response response = ((HXUHFActivity) getActivity()).api
				.writeTypeCTagData(args);
		if (response.result == Response.RESPONSE_PACKET
				&& response.data != null) {
			return DataUtils.toHexString(response.data);
		}
		return "";
	}
}