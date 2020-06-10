package com.olo.service.DataTaskHelper.Beans;

import org.web3j.abi.datatypes.Function;

/**
 * 公共类，针对部分接口只用单个字段
 */
public class CommonDataBean extends DBModel {

    // 余额
    public String ret;
    public Function function;
    public String balance;

    public int nonce;
}
