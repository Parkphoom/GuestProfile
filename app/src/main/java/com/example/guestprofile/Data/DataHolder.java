package com.example.guestprofile.Data;

import com.wacinfo.wacextrathaiid.Data.NativeCardInfo;

public class DataHolder {
  private static NativeCardInfo cardInfo;
  public static NativeCardInfo getCardInfo() {return cardInfo;}
  public static void setCardInfo(NativeCardInfo data) {DataHolder.cardInfo = data;}

  private static DetailInfo detailInfo;
  public static DetailInfo getDetailInfo() {return detailInfo;}
  public static void setDetailInfo(DetailInfo data) {DataHolder.detailInfo = data;}
}