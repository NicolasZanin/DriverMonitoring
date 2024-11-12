package com.drivermonitoring.drivermonitoringsmartwatch.presentation

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import java.util.UUID

class BleServer(private val context: Context) {

    private var bluetoothGattServer: BluetoothGattServer? = null
    private val connectedDevices = mutableSetOf<BluetoothDevice>()

    // UUIDs pour le service et la caractéristique personnalisés
    private val SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb") // Exemple: Device Information Service
    private val CHARACTERISTIC_UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb") // Exemple: Manufacturer Name String

    @SuppressLint("MissingPermission")
    fun startServer() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        // Créer le service GATT
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Créer la caractéristique
        val characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Ajouter la caractéristique au service
        service.addCharacteristic(characteristic)

        // Ajouter le service au serveur GATT
        bluetoothGattServer?.addService(service)
        startAdvertising()
    }

    @SuppressLint("MissingPermission")
    fun stopServer() {
        stopAdvertising()
        bluetoothGattServer?.close()

    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevices.add(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevices.remove(device)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            if (CHARACTERISTIC_UUID == characteristic.uuid) {
                val response = "VotreRéponse".toByteArray(Charsets.UTF_8)
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, response)
            } else {
                bluetoothGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
        }
    }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        advertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true) // Inclure le nom de l'appareil
            .addServiceUuid(ParcelUuid(SERVICE_UUID)) // Optionnel : inclure l'UUID du service
            .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
                // La diffusion a démarré avec succès
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)
                // Gérer les erreurs de diffusion
            }
        }

        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("MissingPermission")
    private fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
    }
}
