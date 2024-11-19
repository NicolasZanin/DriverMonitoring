import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import java.util.*

class BluetoothManager(private val context: Context) {
/*
    private val TAG = "WearOSBluetoothManager"

    private companion object {
        private val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
        private val HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null

    private val discoveredServices = mutableListOf<BluetoothGattService>()
    private val discoveredCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

    fun connectToWearOS() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request BLUETOOTH_CONNECT permission
            return
        }

        scanForWearOSDevices()
        connectToDevice(connectedDevice)
    }

    private fun scanForWearOSDevices() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Request BLUETOOTH_SCAN permission
            return
        }

        bluetoothAdapter.startLeScan { device, _, _ ->
            if (device.name?.startsWith("WearOS") == true) {
                connectedDevice = device
                bluetoothAdapter.stopLeScan(this)
            }
        }
    }

    private fun connectToDevice(device: BluetoothDevice?) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request BLUETOOTH_CONNECT permission
            return
        }

        bluetoothGatt = device?.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to device")
                    gatt.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from device")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Services discovered")
                    discoveredServices.addAll(gatt.services)
                    for (service in discoveredServices) {
                        discoveredCharacteristics.addAll(service.characteristics)
                    }
                    // Now you can interact with the discovered services and characteristics
                    readHeartRateMeasurementCharacteristic()
                } else {
                    Log.d(TAG, "Service discovery failed: $status")
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                super.onCharacteristicRead(gatt, characteristic, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Characteristic read: ${characteristic.uuid}")
                    // Process the read characteristic data
                    processHeartRateMeasurement(characteristic)
                } else {
                    Log.d(TAG, "Characteristic read failed: $status")
                }
            }
        })
    }

    private fun readHeartRateMeasurementCharacteristic() {
        for (characteristic in discoveredCharacteristics) {
            if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHARACTERISTIC_UUID) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // Request BLUETOOTH_CONNECT permission
                    return
                }
                bluetoothGatt?.readCharacteristic(characteristic)
            }
        }
    }*/
}