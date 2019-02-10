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

public class TagReadFragment extends Fragment {

	private Spinner spinnerArea;
	private TextView txtEpc;
	private TextView txtResult;
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
		View rootView = inflater.inflate(R.layout.tag_read_layout, container,
				false);
		spinnerArea = (Spinner) rootView.findViewById(R.id.spinnerArea);
		String[] areas = new String[] { "EPC", "TID", "USER" };

		ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(
				rootView.getContext(), R.layout.simple_list_item, areas);
		areaAdapter.setDropDownViewResource(R.layout.simple_list_item);
		spinnerArea.setAdapter(areaAdapter);

		final Button buttonRead = (Button) rootView
				.findViewById(R.id.buttonRead);
		buttonRead.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonRead.setClickable(false);
				read();
				buttonRead.setClickable(true);
			}
		});

		txtEpc = (TextView) rootView.findViewById(R.id.txtReadEpc);
		txtResult = (TextView) rootView.findViewById(R.id.txtReadResult);
		unmpOffset = (MyunmberinputSpinner) rootView
				.findViewById(R.id.myunmberinputSpinner_offset);
		unmpLength = (MyunmberinputSpinner) rootView
				.findViewById(R.id.myunmberinputSpinner_length);
		editAccesspwd = (EditText) rootView.findViewById(R.id.editAccesspwd);

		return rootView;
	}

	private void read() {
		String ap = editAccesspwd.getText().toString();
		short epcLength = (short) (txtEpc.getText().toString().length() / 2);
		String epc = txtEpc.getText().toString();
		byte mb = (byte) spinnerArea.getSelectedItemPosition();
		switch (mb) {
		case 0:
			mb++;
			break;
		case 1:
			mb++;
			break;
		case 2:
			mb++;
			break;
		default:
			break;
		}
		short sa = Short.parseShort(unmpOffset.getSelectedItem().toString());
		short dl = Short.parseShort(unmpLength.getSelectedItem().toString());
		if (Integer.parseInt(unmpLength.getSelectedItem().toString()) == 0) {
			ToastUtil.showToast(getActivity(), "数据长度不能为0！");
		} else {
			byte[] arguments = Bytes.concat(DataUtils.hexStringTobyte(ap),
					DataUtils.short2byte(epcLength),
					DataUtils.hexStringTobyte(epc), new byte[] { mb },
					DataUtils.short2byte(sa), DataUtils.short2byte(dl));
			String data = readTag(arguments);
			txtResult.setText(data);
			if (!TextUtils.isEmpty(data)) {
				ToastUtil.showToast(getActivity(), "读取成功！");
			} else {
				ToastUtil.showToast(getActivity(), "读取失败！");
			}
		}
	}

	public String readTag(byte[] args) {
		Response response = ((HXUHFActivity) getActivity()).api
				.readTypeCTagData(args);
		if (response.result == Response.RESPONSE_PACKET
				&& response.data != null) {
			return DataUtils.toHexString(response.data);
		}
		return "";
	}
}
