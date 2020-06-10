package com.olo.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import com.olo.service.DataTaskHelper.Beans.CommonBean;
import com.olo.service.DataTaskHelper.Beans.CommonDataBean;
import com.olo.service.DataTaskHelper.http.HttpUtils;
import com.olo.service.util.Transaction;
import com.olo.service.util.TxBody;

public class OLOAccount {

    private String address;// 地址
    private String priKey; // 私钥
    private String pubKey; // 公钥

    private int gasPrice = 100;

    /**
     * @param address 账户地址
     * @param priKey  账户私钥
     * @param pubKey  账户公钥
     */
    public OLOAccount(String address, String priKey, String pubKey) {
        this.address = Numeric.cleanHexPrefix(address);
        this.priKey = Numeric.cleanHexPrefix(priKey);
        this.pubKey = Numeric.cleanHexPrefix(pubKey);
    }

    /**
     * 代币交易和OLO交易
     *
     * @param symbol        币种英文大写简称，如 OLO
     * @param symbolAddress 币种合约地址
     * @param amount        金额, long类型，请将用户输入金额乘以币种的小数位数，如 2.2OLO 为 2.2*10^8
     * @param toAddress     目标地址
     */
    public void trans(final String baseUrl, final String symbol, final String symbolAddress, final String toAddress,
                      final String amount, final String memo, final DataCallback dataCallback) {
        // 首先获取nonce

        CommonBean<CommonDataBean> nonceBean = HttpUtils.requestGet(baseUrl + "/v2/accounts/" + address, "");
        if (nonceBean != null && nonceBean.isSuccess) {
            int nonceInt = nonceBean.result.nonce;
            // 处理转账参数对象
            Transaction tx = new Transaction();
            tx.setCreatedAt(System.currentTimeMillis());
            tx.setGasPrice(gasPrice); // gas价格
            tx.setGasLimit(21000);// gas限额
            tx.setNonce(nonceInt); // 设置 nonce
            tx.setSender(pubKey); // 设置公钥
            TxBody body = new TxBody();
            if ("OLO".equals(symbol.toUpperCase())) { // 如果是OLO币种
                body.setTo(Numeric.cleanHexPrefix(toAddress)); // 设置目标地址
                body.setValue(amount); // 设置转账金额
                body.setLoad("");
            } else {
                // 代币处理签名
                body.setTo(Numeric.cleanHexPrefix(symbolAddress)); // 目标合约地址
                body.setValue("0"); // 设置为0即可
                tx.setGasLimit(100000);// 最大金额
                String methodName = "transfer";

                // 以下代码为ERC20合约转账处理，参考：https://www.netkiller.cn/blockchain/ethereum/web3j/web3j.erc20.html
                List<Type> inputParameters = new ArrayList<>();
                List<TypeReference<?>> outputParameters = new ArrayList<>();
                try {
                    Address tAddress = new Address(Numeric.cleanHexPrefix(toAddress)); // 转账地址
                    Uint256 value = new Uint256(new BigInteger(amount)); // 交易金额
                    inputParameters.add(tAddress);
                    inputParameters.add(value);
                } catch (Exception e) {
                    // 地址异常处理
                    if (dataCallback != null) {
                        dataCallback.callback(DataCallback.DataCode.ERROR_ADDRESS, "地址无效", null);
                    }
                    return;
                }

                TypeReference<Bool> typeReference = new TypeReference<Bool>() {
                };
                outputParameters.add(typeReference);
                Function function = new Function(methodName, inputParameters, outputParameters);
                String encodedFunction = FunctionEncoder.encode(function);

                body.setLoad(Numeric.cleanHexPrefix(encodedFunction));// hex del 0x
            }
            body.setMemo(memo); // 备注
            tx.setBody(body);// 设置body
            tx.setPrivkey(priKey); // 设置私钥
            tx.Sign(); // 签名
            // 执行转账
            CommonBean<String> transBean = HttpUtils.transPost(baseUrl + "/v2/contract/transactions", tx.getJson());
            if (transBean != null && transBean.isSuccess) {
                if (dataCallback != null) {
                    dataCallback.callback(DataCallback.DataCode.SUCCESS, "转账成功", transBean.result, tx.TxHash());
                }
            } else {
                String msg = transBean.message;
                if (dataCallback != null) {
                    dataCallback.callback(DataCallback.DataCode.FAILED, msg, transBean.result, tx.TxHash());
                }
            }
        }

    }

    /**
     * 交易OLO
     *
     * @param baseUrl OLO API URL
     * @param toAddress 目标地址
     * @param amount    金额, long类型，请将用户输入金额乘以币种的小数位数，如 2.2OLO 为 2.2*10^8
     * @param memo tx comment,optional
     */
    public void transOLO(String baseUrl, String toAddress, String amount, String memo, DataCallback dataCallback) {
        trans(baseUrl, "OLO", "", toAddress, amount, memo, dataCallback);
    }

    /**
     * 交易ERC20 COIN BASE OLO
     *
     * @param baseUrl OLO API URL
     * @param contractAddress ERC20 COIN contract address
     * @param toAddress 目标地址
     * @param amount    金额, long类型，请将用户输入金额乘以币种的小数位数，如 2.2 XDF 为 2.2*10^4
     * @param memo tx comment,optional
     */
    public void transERC20(String baseUrl, String contractAddress, String toAddress, String amount, String memo, DataCallback dataCallback) {
        trans(baseUrl, "", contractAddress, toAddress, amount, memo, dataCallback);
    }
}
