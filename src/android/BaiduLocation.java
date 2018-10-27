package com.runchain.plugins.baidu;

import android.app.Activity;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.location.Poi;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ionic 百度定位插件 for android
 *
 * @author ge_pingping
 */
public class BaiduLocation extends CordovaPlugin {

  /**
   * LOG TAG
   */
  private static final String LOG_TAG = BaiduLocation.class.getSimpleName();

  /**
   * JS回调接口对象
   */
  public static CallbackContext cbCtx = null;

  /**
   * 百度定位客户端
   */
  public LocationClient mLocationClient = null;

  public boolean stopListen = true;

  /**
   * 百度定位监听
   */
  public BDAbstractLocationListener myListener = new BDAbstractLocationListener() {

    /**
     * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
     * 自动回调，相同的diagnosticType只会回调一次
     *
     * @param locType           当前定位类型
     * @param diagnosticType    诊断类型（1~9）
     * @param diagnosticMessage 具体的诊断信息释义
     */
    @Override
    public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {

      Activity activity = cordova.getActivity();

      if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_GPS) {

        //建议打开GPS
        Toast.makeText(activity, "请打开GPS！", Toast.LENGTH_SHORT).show();

      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_WIFI) {

        //建议打开wifi，不必连接，这样有助于提高网络定位精度！
        Toast.makeText(activity, "请打开wifi，这样有助于提高网络定位精度！", Toast.LENGTH_SHORT).show();


      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_LOC_PERMISSION) {

        //定位权限受限，建议提示用户授予APP定位权限！
        Toast.makeText(activity, "请授予应用定位权限！", Toast.LENGTH_SHORT).show();


      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_NET) {

        //网络异常造成定位失败，建议用户确认网络状态是否异常！
        Toast.makeText(activity, "请确认网络状态是否异常！", Toast.LENGTH_SHORT).show();

      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CLOSE_FLYMODE) {

        //手机飞行模式造成定位失败，建议用户关闭飞行模式后再重试定位！
        Toast.makeText(activity, "请先关闭飞行模式！", Toast.LENGTH_SHORT).show();


      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_INSERT_SIMCARD_OR_OPEN_WIFI) {

        //无法获取任何定位依据，建议用户打开wifi或者插入sim卡重试！
        Toast.makeText(activity, "请打开wifi或者插入sim卡重试！", Toast.LENGTH_SHORT).show();

      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_OPEN_PHONE_LOC_SWITCH) {

        //无法获取有效定位依据，建议用户打开手机设置里的定位开关后重试！
        Toast.makeText(activity, "请设置里的定位开关后重试！", Toast.LENGTH_SHORT).show();

      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_SERVER_FAIL) {

        //百度定位服务端定位失败
        //建议反馈location.getLocationID()和大体定位时间到loc-bugs@baidu.com

      } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_FAIL_UNKNOWN) {

        //无法获取有效定位依据，但无法确定具体原因
        //建议检查是否有安全软件屏蔽相关定位权限
        //或调用LocationClient.restart()重新启动后重试！

        Toast.makeText(activity, "无法获取有效定位依据，请稍后再试！", Toast.LENGTH_SHORT).show();

      }
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
      LOG.d(LOG_TAG, "location received..");
      String code = "0";

      String message = "";

      JSONObject resultObj = new JSONObject();

      try {
        JSONObject json = new JSONObject();

        json.put("time", location.getTime());
        json.put("locType", location.getLocType());
        json.put("latitude", location.getLatitude());
        json.put("longitude", location.getLongitude());
        json.put("radius", location.getRadius());
        json.put("address", location.getAddrStr());

        json.put("locationID", location.getLocationID());
        json.put("country", location.getCountry());
        json.put("countryCode", location.getCountryCode());
        json.put("city", location.getCity());
        json.put("cityCode", location.getCityCode());
        json.put("district", location.getDistrict());
        json.put("street", location.getStreet());
        json.put("streetNumber", location.getStreetNumber());
        json.put("locationDescribe", location.getLocationDescribe());

        List<Poi> poiList = location.getPoiList();
        JSONArray poiArray = new JSONArray();
        for (Poi poi : poiList) {
          JSONObject poiObj = new JSONObject();
          poiObj.put("id", poi.getId());
          poiObj.put("name", poi.getName());
          poiObj.put("rank", poi.getRank());
          poiArray.put(poiObj);
        }
        json.put("poiList", poiArray);
        json.put("buildingID", location.getBuildingID());
        json.put("buildingName", location.getBuildingName());
        json.put("floor", location.getFloor());

        if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
          json.put("speed", location.getSpeed());
          json.put("satellite", location.getSatelliteNumber());
          json.put("height", location.getAltitude());
          json.put("direction", location.getDirection());
          json.put("describe", "GPS定位成功");
        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
          json.put("operators", location.getOperators());
          json.put("describe", "网络定位成功");
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
          json.put("describe", "离线定位成功，离线定位结果也是有效的");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
          json.put("describe", "服务端网络定位失败，可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com");
          code = "-1";
          message = "服务端网络定位失败";
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
          json.put("describe", "网络不同导致定位失败，请检查网络是否通畅");
          code = "-1";
          message = "网络不通导致定位失败，请检查网络是否通畅";
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
          json.put("describe", "请授予应用定位权限");
          code = "-1";
          message = "请授予应用定位权限";
        }

        resultObj.put("code", code);

        resultObj.put("message", message);

        resultObj.put("data", json);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
        pluginResult.setKeepCallback(true);
        cbCtx.sendPluginResult(pluginResult);
      } catch (JSONException e) {
        String errMsg = e.getMessage();
        LOG.e(LOG_TAG, errMsg, e);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, errMsg);
        pluginResult.setKeepCallback(true);
        cbCtx.sendPluginResult(pluginResult);
      } finally {
        if (stopListen)
          mLocationClient.stop();
      }
    }

  };

  /**
   * 插件主入口
   */
  @Override
  public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
    LOG.d(LOG_TAG, "Baidu Location #execute");

    boolean ret = false;
    cbCtx = callbackContext;

    if ("getCurrentPosition".equalsIgnoreCase(action)) {
      stopListen = true;
      if (mLocationClient == null) {
        mLocationClient = new LocationClient(this.webView.getContext());
        mLocationClient.registerLocationListener(myListener);
      }
      // 配置定位SDK参数
      initLocation(0);
      if (mLocationClient.isStarted())
        mLocationClient.stop();
      mLocationClient.start();
      ret = true;
    } else if ("watchPosition".equalsIgnoreCase(action)) {
      stopListen = false;
      if (mLocationClient == null) {
        mLocationClient = new LocationClient(this.webView.getContext());
        mLocationClient.registerLocationListener(myListener);
      }

      int span = args.getInt(0);

      // 配置定位SDK参数
      initLocation(span * 1000);
      if (mLocationClient.isStarted())
        mLocationClient.stop();
      mLocationClient.start();
      ret = true;
    } else if ("clearWatch".equalsIgnoreCase(action)) {
      if (mLocationClient != null && mLocationClient.isStarted())
        mLocationClient.stop();
      ret = true;
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
      pluginResult.setKeepCallback(false);
      cbCtx.sendPluginResult(pluginResult);
    }
    return ret;
  }

  /**
   * 配置定位SDK参数
   */
  private void initLocation(int span) {
    LocationClientOption option = new LocationClientOption();
    // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
    option.setLocationMode(LocationMode.Hight_Accuracy);
    // 可选，默认gcj02，设置返回的定位结果坐标系
    option.setCoorType("bd09ll");
    // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
    if (stopListen)
      option.setScanSpan(0);
    else
      option.setScanSpan(span);
    // 可选，设置是否需要地址信息，默认不需要
    option.setIsNeedAddress(true);
    // 可选，默认false,设置是否使用gps
    option.setOpenGps(true);
    // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.setLocationNotify(true);
    /* 可选，默认false，设置是否需要位置语义化结果，
     * 可以在BDLocation.getLocationDescribe里得到，
     * 结果类似于“在北京天安门附近”
     */
    option.setIsNeedLocationDescribe(true);
    // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
    option.setIsNeedLocationPoiList(true);
    // 可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
    // option.setIgnoreKillProcess(false);
    // 可选，默认false，设置是否收集CRASH信息，默认收集
    // option.SetIgnoreCacheException(true);
    // 可选，默认false，设置是否需要过滤gps仿真结果，默认需要
    // option.setEnableSimulateGps(false);
    mLocationClient.setLocOption(option);
  }

  @Override
  public void onDestroy() {
    if (mLocationClient != null && mLocationClient.isStarted()) {
      mLocationClient.stop();
      mLocationClient = null;
    }
    super.onDestroy();
  }


}
