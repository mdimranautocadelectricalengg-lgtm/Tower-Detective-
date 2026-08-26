package com.towerdetective.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityWcdma;
import android.telephony.TelephonyManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import java.util.List;

@CapacitorPlugin(
    name = "TowerDetector",
    permissions = {
        @Permission(
            alias = "location",
            strings = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }
        ),
        @Permission(
            alias = "phone",
            strings = {
                Manifest.permission.READ_PHONE_STATE
            }
        )
    }
)
public class TowerDetectorPlugin extends Plugin {

    private TelephonyManager telephonyManager;

    @Override
    public void load() {
        telephonyManager =
            (TelephonyManager) getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {

        requestPermissionForAlias(
            "location",
            call,
            "locationPermissionCallback"
        );
    }

    @PluginMethod
    public void getCellInfo(PluginCall call) {

        try {

            if (telephonyManager == null) {
                load();
            }

            if (
                getContext().checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                call.reject("Location permission required");
                return;
            }

            JSObject result = new JSObject();

            String operator =
                telephonyManager.getNetworkOperatorName();

            result.put(
                "operator",
                operator == null || operator.isEmpty()
                    ? "Unknown"
                    : operator
            );

            result.put(
                "networkType",
                networkTypeName(
                    telephonyManager.getDataNetworkType()
                )
            );

            com.getcapacitor.JSArray cells =
                new com.getcapacitor.JSArray();

            List<CellInfo> info =
                telephonyManager.getAllCellInfo();

            if (info != null) {

                for (CellInfo cell : info) {

                    JSObject item =
                        convertCell(cell);

                    cells.put(item);
                }
            }

            result.put("cells", cells);

            call.resolve(result);

        } catch (SecurityException e) {

            call.reject(
                "Permission denied: " + e.getMessage()
            );

        } catch (Exception e) {

            call.reject(
                "Cell scan failed: " + e.getMessage()
            );
        }
    }

    private JSObject convertCell(CellInfo info) {

        JSObject obj = new JSObject();

        obj.put(
            "registered",
            info.isRegistered()
        );

        obj.put(
            "signalDbm",
            info.getCellSignalStrength().getDbm()
        );

        if (info instanceof CellInfoLte) {

            CellIdentityLte id =
                ((CellInfoLte) info).getCellIdentity();

            obj.put("type", "LTE");
            obj.put("cellId", id.getCi());
            obj.put("tac", id.getTac());
            obj.put("pci", id.getPci());
            obj.put("mcc", id.getMccString());
            obj.put("mnc", id.getMncString());

        } else if (
            Build.VERSION.SDK_INT >= 29 &&
            info instanceof CellInfoNr
        ) {

            CellIdentityNr id =
                (CellIdentityNr)
                    ((CellInfoNr) info).getCellIdentity();

            obj.put("type", "5G NR");
            obj.put("cellId", id.getNci());
            obj.put("tac", id.getTac());
            obj.put("pci", id.getPci());
            obj.put("mcc", id.getMccString());
            obj.put("mnc", id.getMncString());

        } else if (info instanceof CellInfoGsm) {

            CellIdentityGsm id =
                ((CellInfoGsm) info).getCellIdentity();

            obj.put("type", "GSM");
            obj.put("cellId", id.getCid());
            obj.put("tac", id.getLac());
            obj.put("pci", id.getPsc());
            obj.put("mcc", id.getMccString());
            obj.put("mnc", id.getMncString());

        } else if (info instanceof CellInfoWcdma) {

            CellIdentityWcdma id =
                ((CellInfoWcdma) info).getCellIdentity();

            obj.put("type", "WCDMA");
            obj.put("cellId", id.getCid());
            obj.put("tac", id.getLac());
            obj.put("pci", id.getPsc());
            obj.put("mcc", id.getMccString());
            obj.put("mnc", id.getMncString());
        }

        return obj;
    }

    private String networkTypeName(int type) {

        switch (type) {

            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "2G GPRS";

            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2G EDGE";

            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G";

            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G LTE";

            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G NR";

            default:
                return "Unknown";
        }
    }
}
