package simpleBlockchainV2;

import java.util.Base64;

public class test{
    //public byte[] signature = {48,53,2,24,88,123,5,33,48,-32,8,63,-99,-29,-1,35,-7,111,-73,122,27,73,99,-62,-76,100,18,-86,2,25,0,-17,-119,1,87,53,-128,-8,105,79,-37,-108,-22,51,-30,-90,-43,-103,-42,-64,-115,-69,39,52,-56};

    public static void main(String[] args) {
        byte[] signature = { 48, 53, 2, 24, 88, 123, 5, 33, 48, -32, 8, 63, -99, -29, -1, 35, -7, 111, -73, 122, 27, 73,
                99, -62, -76, 100, 18, -86, 2, 25, 0, -17, -119, 1, 87, 53, -128, -8, 105, 79, -37, -108, -22, 51, -30,
                -90, -43, -103, -42, -64, -115, -69, 39, 52, -56 };
        System.out.println(Base64.getEncoder().encode(signature));
    }
}