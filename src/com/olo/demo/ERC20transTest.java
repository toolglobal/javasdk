package com.olo.demo;

import com.olo.service.DataCallback;
import com.olo.service.OLOAccount;

public class ERC20transTest {
    public static void main(String[] args) {
        // 请先申请账户。
        String apiUrl = "http://192.168.8.145:10000"; // api地址 without suffix "/"
        String address = "0x0F508F143E77b39F8e20DD9d2C1e515f0f527D9F"; // 地址
        String priKey = "7fffe4e426a6772ae8a1c0f2425a90fc6320d23e416fb6d83802889fa846faa2"; // 私钥
        String pubKey = "03815a906de2017c7351be33644cd60a6fff9407ce04896b2328944bc4e628abd8"; // 公钥

        OLOAccount oloAccount = new OLOAccount(address, priKey, pubKey);

        // 例如： 交易 0.002 ABC 为 0.002*10^8=200000 ，
        // 0xe1066eBcFC8fbD7172886F15F538b63804676A74 contract address of ABC Coin
        // 目标地址为：0x8c5015F85B993243A80478D53bc951f84F553dBB
        oloAccount.transERC20(apiUrl, "0xe1066eBcFC8fbD7172886F15F538b63804676A74","0x8c5015F85B993243A80478D53bc951f84F553dBB", "300000", "test",
                new DataCallback() {
                    @Override
                    public void callback(DataCode code, String message, Object data, String txHash) {
                        if (data != null) {
                            System.out.printf("code=%s msg=%s tx=%s data=%s\n", code.toString(), message, txHash, data.toString());
                        } else {
                            System.out.printf("code=%s msg=%s tx=%s \n", code.toString(), message, txHash);
                        }
                    }
                });

    }
}
