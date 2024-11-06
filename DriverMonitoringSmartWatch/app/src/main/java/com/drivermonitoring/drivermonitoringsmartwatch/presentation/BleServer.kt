package com.drivermonitoring.drivermonitoringsmartwatch.presentation

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.util.Log
import java.util.UUID

class BleServer(private val context: Context) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGattServer: BluetoothGattServer? = null
    private val bluetoothLeAdvertiser: BluetoothLeAdvertiser? =
        bluetoothAdapter.bluetoothLeAdvertiser

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Handle connected device
                Log.d("BLE_SERVER", "Le téléphone est connecté à cette montre !");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Handle disconnected device
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            // Handle characteristic read request
            bluetoothGattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                byteArrayOf()
            )
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            // Handle characteristic write request
            bluetoothGattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                value
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        // Nommer le serveur BLE
        bluetoothAdapter.name = "Driver_monitoring"

        // Récupérer et afficher l'adresse MAC
        val macAddress = bluetoothAdapter.address
        Log.d("BLE_SERVER", "Adresse MAC : $macAddress")

        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        // Create and add service to the GATT server
        val service = BluetoothGattService(
            UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        bluetoothGattServer?.addService(service)

        // Create and add characteristics to the service
        val characteristic = BluetoothGattCharacteristic(
            UUID.randomUUID(),
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristic)
        //characteristic.value =
            //bluetoothGattServer?.notifyCharacteristicChanged(null, characteristic, false)

        // Start advertising the BLE server
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()

        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .build()

        bluetoothLeAdvertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            // Advertising started successfully
        }

        override fun onStartFailure(errorCode: Int) {
            // Advertising failed to start
        }
    }
}