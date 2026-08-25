package com.towerdetective.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(
    name = "TowerDetector",
    permissions = {
        @com.getcapacitor.annotation.Permission(
            alias = "location",
            strings = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }
        )
    }
)
public class TowerDetectorPlugin extends Plugin {

    private TelephonyManager telephonyManager;


    @Override
    public void load() {

        telephonyManager =
            (TelephonyManager)
            getContext().getSystemService(
                Context.TELEPHONY_SERVICE
            );
    }


    @PluginMethod
    public void requestPermissions(
        PluginCall call
    ) {

        requestPermissionForAlias(
            "location",
            call,
            "locationPermissionCallback"
        );
    }


    @PluginMethod
    public void getCellInfo(
        PluginCall call
    ) {

        try {

            if (telephonyManager == null) {
                load();
            }


            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M
            ) {

                if (
                    getContext().checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    call.reject(
                        "Location permission is required"
                    );

                    return;
                }
            }


            JSObject result =
                new JSObject();


            result.put(
                "operator",
                getOperator()
            );


            result.put(
                "networkType",
                getNetworkType()
            );


            JSArray cells =
                new JSArray();


            List<CellInfo> list =
                telephonyManager.getAllCellInfo();


            if(list != null){

                for(CellInfo info : list){

                    JSObject cell =
                        convertCell(info);

                    if(cell != null){

                        cells.put(cell);

                    }
                }
            }


            result.put(
                "cells",
                cells
            );


            call.resolve(result);


        } catch(SecurityException e){

            call.reject(
                "Android permission denied"
            );


        } catch(Exception e){

            call.reject(
                "Cell scan failed: " +
                e.getMessage()
            );

        }
    }


    private JSObject convertCell(
        CellInfo info
    ){

        JSObject obj =
            new JSObject();


        obj.put(
            "registered",
            info.isRegistered()
        );


        CellSignalStrength strength =
            info.getCellSignalStrength();


        obj.put(
            "signalDbm",
            strength.getDbm()
        );


        if(info instanceof CellInfoLte){

            CellIdentityLte id =
                ((CellInfoLte)info)
                .getCellIdentity();


            obj.put("type","LTE");

            obj.put(
                "cellId",
                id.getCi()
            );

            obj.put(
                "tac",
                id.getTac()
            );

            obj.put(
                "pci",
                id.getPci()
            );

            obj.put(
                "mcc",
                id.getMccString()
            );

            obj.put(
                "mnc",
                id.getMncString()
            );

        }


        else if(
            Build.VERSION.SDK_INT >= 29 &&
            info instanceof CellInfoNr
        ){

            CellIdentityNr id =
                (CellIdentityNr)
                ((CellInfoNr)info)
                .getCellIdentity();


            obj.put(
                "type",
                "5G NR"
            );

            obj.put(
                "cellId",
                id.getNci()
            );

            obj.put(
                "tac",
                id.getTac()
            );

            obj.put(
                "pci",
                id.getPci()
            );

            obj.put(
                "mcc",
                id.getMccString()
            );

            obj.put(
                "mnc",
                id.getMncString()
            );

        }


        else if(info instanceof CellInfoGsm){

            CellIdentityGsm id =
                ((CellInfoGsm)info)
                .getCellIdentity();


            obj.put(
                "type",
                "GSM"
            );

            obj.put(
                "cellId",
                id.getCid()
            );

            obj.put(
                "tac",
                id.getLac()
            );

            obj.put(
                "pci",
                id.getPsc()
            );

            obj.put(
                "mcc",
                id.getMccString()
            );

            obj.put(
                "mnc",
                id.getMncString()
            );

        }


        else if(info instanceof CellInfoWcdma){

            CellIdentityWcdma id =
                ((CellInfoWcdma)info)
                .getCellIdentity();


            obj.put(
                "type",
                "WCDMA"
            );

            obj.put(
                "cellId",
                id.getCid()
            );

            obj.put(
                "tac",
                id.getLac()
            );

            obj.put(
                "pci",
                id.getPsc()
            );

            obj.put(
                "mcc",
                id.getMccString()
            );

            obj.put(
                "mnc",
                id.getMncString()
            );

        }


        else if(info instanceof CellInfoCdma){

            CellIdentityCdma id =
                ((CellInfoCdma)info)
                .getCellIdentity();


            obj.put(
                "type",
                "CDMA"
            );

            obj.put(
                "cellId",
                id.getBasestationId()
            );

            obj.put(
                "tac",
                id.getNetworkId()
            );

            obj.put(
                "pci",
                id.getSystemId()
            );

        }


        return obj;
    }


    private String getOperator(){

        try{

            String name =
                telephonyManager
                .getNetworkOperatorName();


            if(
                name == null ||
                name.isEmpty()
            ){

                return "Unknown";

            }


            return name;


        }catch(Exception e){

            return "Unknown";

        }

    }


    private String getNetworkType(){

        try{

            int type =
                telephonyManager
                .getDataNetworkType();


            switch(type){

                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return "2G GPRS";

                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return "2G EDGE";

                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return "3G";

                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return "3G HSDPA";

                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return "3G HSUPA";

                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return "3G HSPA";

                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G LTE";

                case TelephonyManager.NETWORK_TYPE_NR:
                    return "5G NR";

                default:
                    return "Unknown";
            }


        }catch(Exception e){

            return "Unknown";

        }

    }

}
