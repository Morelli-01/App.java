package MainProject.NFTClasses;

import MainProject.Utils.JSONParser;
import kong.unirest.Unirest;

/*
 * This class is the rapresentation of a single NFT
 */

public class NFT_Object {
    private String NftToken;
    private String MEurl;
    private String price=String.valueOf(Double.MAX_VALUE);
    private String collectionName;
    private String objName;

    public NFT_Object(){}
    public NFT_Object(String nftToken, String MEurl, String price, String collectionName) {
        NftToken = nftToken;
        this.MEurl = MEurl;
        this.price = price;
        this.collectionName = collectionName;
    }
    public NFT_Object(String nftToken,  String price, String collectionName) {

        NftToken = nftToken;
        this.MEurl = "https://magiceden.io/item-details/"+this.NftToken;
        this.price = price;
        this.collectionName = collectionName;
        /*objName = JSONParser.parseFromString(Unirest.get("https://api-mainnet.magiceden.io/rpc/getNFTByMintAddress/"+
                this.NftToken +"?useRarity=true").asString().getBody(), "title");*/
        objName = JSONParser.parseFromString(Unirest.get("https://public-api.solscan.io/token/meta?tokenAddress="+this.NftToken).asString().getBody(), "name");

    }


    public String getMEurl() {
        return MEurl;
    }

    public void setMEurl(String MEurl) {
        this.MEurl = MEurl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setNftToken(String nftToken) {
        NftToken = nftToken;
    }

    public String getNftToken() {
        return NftToken;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public String getObjName() {
        return objName;
    }

    @Override
    public String toString() {
        return "NFT_Object{" +
                "NftToken='" + NftToken + '\'' +
                ", MEurl=" + MEurl +
                ", price='" + price + '\'' +
                ", collectionName='" + collectionName + '\'' +
                '}';
    }
}
