package com.olo.service.util;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Hu Dingjiang
 * @date 2019/7/1
 */
public class TxBody {
	private String to;
	private String value;
	private String load;// hex
	private String memo;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLoad() {
		return load;
	}

	public void setLoad(String load) {
		this.load = load;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public JSONObject getJson() {
		JSONObject result = new JSONObject();
		try {
			result.put("to", to);
			result.put("value", value);
			result.put("load", load);
			result.put("memo", memo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
