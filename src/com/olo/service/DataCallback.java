package com.olo.service;

/**
 * @author Hu Dingjiang
 * @date 2020/2/11
 */
public abstract class DataCallback {
	public void callback(DataCode code, String message, Object data, String txHash) {
	}

	public void callback(DataCode code, String message, Object data) {
		callback(code, message, data, null);
	}

	protected enum DataCode {
		SUCCESS, ERROR_ADDRESS, FAILED
	}
}
