package np.com.naxa.drone_tasking_manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.viewmodels.states.DownloadAndTransferState
import np.com.naxa.drone_tasking_manager.features.device_connection.viewmodels.states.UsbDeviceState
import np.com.naxa.drone_tasking_manager.app.DroneTMApp
import np.com.naxa.drone_tasking_manager.utils.FileTransferHandler
import np.com.naxa.drone_tasking_manager.utils.PermissionUtils
import np.com.naxa.drone_tasking_manager.utils.toFile
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.viewmodels.DownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.EventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.NavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.features.device_connection.viewmodels.UsbDeviceViewModel
import java.io.File


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val deviceViewModel by viewModels<UsbDeviceViewModel>()
    private val eventsViewModel by viewModels<EventsViewModel>()
    private val downloadAndTransferViewModel by viewModels<DownloadAndTransferFileViewModel> {
        DownloadAndTransferFileViewModel.Factory
    }
    private val navigationEventsViewModel by viewModels<NavigationEventsViewModel>()


    private val usbManager by lazy { getSystemService(Context.USB_SERVICE) as UsbManager }
    private var usbDeviceConnection: UsbDeviceConnection? = null
    private var _deviceFetchingJob: Job? = null

    /**
     * Permission launcher for all files access
     */
    private val allFilesStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if permission is granted
            if (Environment.isExternalStorageManager()) {
                // Permission granted, proceed with file operations
                fetchingConnectedUsbDeviceIfAny()

            } else {
                // Permission denied
                Toast.makeText(
                    this@MainActivity,
                    "You have denied all file access permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Permission launcher for handling storage permission request
     */
    private val mediaFilesStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        val writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

        when {
            readPermissionGranted && writePermissionGranted -> {
                // Both read and write permissions granted
                // Proceed with file operations
                fetchingConnectedUsbDeviceIfAny()
            }

            !readPermissionGranted && !writePermissionGranted -> {
                // If not granted both permission
                Toast.makeText(
                    this@MainActivity,
                    "You have denied both read and write permission",
                    Toast.LENGTH_LONG
                ).show()
            }

            !readPermissionGranted -> {
                // If not granted read permission only
                Toast.makeText(
                    this@MainActivity,
                    "You have denied read permission",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                // If not granted write permission only
                Toast.makeText(
                    this@MainActivity,
                    "You have denied write permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Launcher for directory access using the GetContent contract.
     * This allows the user to select a directory, and the result is provided as a URI.
     */
    private val directoryAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    Log.d(TAG, "directoryAccessLauncher: $uri")

                    // Persist the permissions
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    // Access Granted
                    // downloadAndTransferViewModel.startTransfer(
                    //     destinationUri = FileTransferHandler.djiWaypointUri(
                    //         this@MainActivity,
                    //         uri
                    //     ) ?: uri
                    // )

                    val destinationUri = FileTransferHandler.djiWaypointUri(
                        this@MainActivity,
                        uri
                    ) ?: uri

                    DocumentFile.fromTreeUri(this@MainActivity, destinationUri)?.let {
                        downloadAndTransferViewModel.update(
                            DownloadAndTransferState.SafDirectorySelected(
                                it
                            )
                        )
                    }

                } catch (e: Exception) {
                    Log.d(
                        TAG,
                        "directoryAccessLauncher: Failed to access selected directory (${e.message})"
                    )
                }
            }
        } else {
            Log.d(TAG, "directoryAccessLauncher: Storage access denied")
        }
    }

    /**
     * Launcher for picking a single file from the device's storage using the GetContent contract.
     * This allows the user to select a file, and the result is provided as a URI.
     */
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Update the state with the Uri
            try {
                DocumentFile.fromSingleUri(this@MainActivity, it)?.let {
                    lifecycleScope.launch {
                        val file = it.toFile(this@MainActivity) ?: return@launch
                        downloadAndTransferViewModel.update(
                            DownloadAndTransferState.DownloadCompleted(
                                file
                            )
                        )
                    }
                }

            } catch (e: Exception) {
                Log.d(TAG, "pickPictureLauncher: ${e.message}")
            }
        }
    }

    /**
     * Called when the activity is starting.
     *
     * This method is part of the Android activity lifecycle and is where the activity
     * initializes its user interface and prepares for user interaction. It sets up
     * the content view and registers necessary components for handling USB device
     * permissions and events.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down, this Bundle contains
     *                           the data it most recently supplied in
     *                           onSaveInstanceState(Bundle). Otherwise, it is null.
     */
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            DroneTMApp(
                deviceViewModel = deviceViewModel,
                eventsViewModel = eventsViewModel,
                downloadAndTransferViewModel = downloadAndTransferViewModel,
                navigationEventsViewModel = navigationEventsViewModel,
            )
        }

        // Register usb device permission receiver
        registerUsbDevicePermissionReceiver()

        // Collects events from the composable
        collectEventsFromComposableFunctionsAndBehaveAccordingly()

        // Handling intent
        handleIntent(intent)
    }


    /**
     * Collects events from the Composable functions and performs actions based on the events received.
     *
     * This method launches a coroutine that listens for application events emitted by the
     * `eventsViewModel`. It operates within the lifecycle scope of the activity, ensuring that
     * it only collects events when the activity is in the STARTED state. For each event collected,
     * it displays a toast message and executes the corresponding action based on the event type.
     *
     * The following events are handled:
     * - OnStoragePermissionRequested: Triggers a check and request for storage-related permissions.
     * - OnUsbDeviceClick: Initiates the handling of a USB device click event.
     * - OnUsbDeviceRefreshClick: Refreshes the list of USB devices.
     * - FetchedUsbDevices: Also triggers a refresh of the list of USB devices.
     */
    private fun collectEventsFromComposableFunctionsAndBehaveAccordingly() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventsViewModel.appEvents.collect { event ->
                    when (event) {
                        is DroneTMAppEvent.OnStoragePermissionRequested -> {
                            checkAndRequestStorageRelatedPermissions()
                        }

                        is DroneTMAppEvent.OnDownloadInitiated -> {
                            downloadAndTransferViewModel.setFile(file = null)
                            downloadAndTransferViewModel.startDownload(
                                url = event.url
                            )
                        }

                        is DroneTMAppEvent.OnTransferInitiated -> {
                            // Not handling here, It will be handle in
                            // directoryAccessLauncher
                        }

                        is DroneTMAppEvent.OnDroneTMDirectoryAccessRequested -> {
                            downloadAndTransferViewModel.setFile(file = event.file)
                            requestDirectoryAccess(event.file)
                        }

                        DroneTMAppEvent.OnFilePickerRequested -> {
                            downloadAndTransferViewModel.setFile(file = null)
                            filePickerLauncher.launch("*/*")
                        }

                        DroneTMAppEvent.OnUsbDeviceSearchRequested -> {
                            fetchingConnectedUsbDeviceIfAny()
                        }
                    }
                }
            }
        }
    }

    /**
     * Broadcast receiver for the usb device attached, detached and permission action
     */
    private val usbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleIntent(intent)
        }
    }

    /**
     * Handles USB device attachment, detachment, and permission actions.
     *
     * This function processes the incoming intent to determine the action related to USB devices.
     * It handles three main actions:
     * 1. ACTION_USB_DEVICE_ATTACHED: Fetches the list of USB devices.
     * 2. ACTION_USB_DEVICE_DETACHED: Cleans up and closes communication with the detached USB device.
     * 3. ACTION_USB_DEVICE_PERMISSION: Manages the permission request for USB device communication.
     *
     * @param intent The intent containing the action and associated data for the USB device event.
     */
    private fun handleIntent(intent: Intent) {
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
            fetchingConnectedUsbDeviceIfAny()
            return
        }

        if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
            val device: UsbDevice? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        UsbManager.EXTRA_DEVICE,
                        UsbDevice::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                }

            device?.let {
                // call your method that cleans up and closes communication with the device
                val usbInterface = if (it.interfaceCount > 0) it.getInterface(0) else null

                if (usbInterface != null) {
                    usbDeviceConnection?.releaseInterface(usbInterface)
                }

                usbDeviceConnection?.close()
            }

            fetchingConnectedUsbDeviceIfAny(
                detached = true
            )
            return
        }

        if (ACTION_USB_DEVICE_PERMISSION == intent.action) {
            synchronized(this) {
                val device: UsbDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            UsbManager.EXTRA_DEVICE,
                            UsbDevice::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    }

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.let {
                        // call method to set up device communication
                    }
                } else {
                    Log.d(TAG, "permission denied for device $device")
                }
            }
            return
        }
    }

    /**
     * Fetches the list of connected USB devices, if any.
     *
     * This function is called when the app starts and whenever the user plugs in or unplugs a USB device.
     * It updates the [deviceViewModel] with the connected device or an error message if
     * there was a problem fetching the devices.
     */
    private fun fetchingConnectedUsbDeviceIfAny(
        detached: Boolean = false,
        forVerifyingStableConnection: Boolean = false
    ) {

        if (_deviceFetchingJob != null) {
            if (_deviceFetchingJob!!.isActive) {
                _deviceFetchingJob?.cancel("Re-fetching by cancelling previous job")
            }
        }

        _deviceFetchingJob = lifecycleScope.launch {
            try {
                // Handling and updating initial state before searching for connected device
                when (val state = deviceViewModel.deviceState.value) {
                    is UsbDeviceState.Connected -> {
                        if (state.stable) {
                            if (isActive) deviceViewModel.connected(state.device, stable = false)
                        }
                    }

                    else -> {
                        if (isActive) deviceViewModel.waitingToConnect()
                    }
                }

                // Device searching / listing
                val devices = usbManager.deviceList.values.toMutableList()

                // If device is not empty
                // Update states accordingly
                if (devices.isNotEmpty()) {
                    val device = devices.first()

                    when (val state = deviceViewModel.deviceState.value) {
                        is UsbDeviceState.Connected -> {
                            val lastDevice = state.device

                            if (forVerifyingStableConnection) {
                                if (isActive) {
                                    deviceViewModel.connected(
                                        device,
                                        lastDevice.deviceName == device.deviceName
                                    )
                                }
                            } else {
                                if (isActive) {
                                    deviceViewModel.connected(device, false)

                                    delay(5000)
                                    if (isActive) {
                                        fetchingConnectedUsbDeviceIfAny(forVerifyingStableConnection = true)
                                    }
                                }
                            }

                        }

                        else -> {
                            if (isActive) {
                                deviceViewModel.connected(device, stable = false)

                                delay(5000)
                                if (isActive) {
                                    fetchingConnectedUsbDeviceIfAny(forVerifyingStableConnection = true)
                                }
                            }
                        }
                    }

                    return@launch
                }

                // Else if list is empty and reconnected is more than zero
                if (detached) {
                    delay(2000)
                    if (isActive) deviceViewModel.waitingToConnect()
                }

            } catch (e: Exception) {
                if (isActive) {
                    deviceViewModel.error(e.message ?: e.cause?.message ?: "Unable to load devices")
                }
            }
        }
    }

    /**
     * Registers a broadcast receiver for USB device permission events.
     *
     * This method creates an IntentFilter to listen for specific USB-related actions:
     * 1. ACTION_USB_DEVICE_PERMISSION: Triggered when a USB device permission request is made.
     * 2. ACTION_USB_DEVICE_ATTACHED: Triggered when a USB device is attached to the device.
     * 3. ACTION_USB_DEVICE_DETACHED: Triggered when a USB device is detached from the device.
     *
     * The method then registers the `usbDeviceReceiver` to handle these actions.
     * The receiver is registered with the `ContextCompat.RECEIVER_NOT_EXPORTED` flag,
     * ensuring that the receiver is not accessible to other applications.
     */
    private fun registerUsbDevicePermissionReceiver() {
        val filter = IntentFilter()
            .apply {
                addAction(ACTION_USB_DEVICE_PERMISSION)
                addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            }
        ContextCompat.registerReceiver(
            this@MainActivity,
            usbDeviceReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    /**
     * Requests permission to communicate with a USB device.
     *
     * This method creates a `PendingIntent` that will be triggered when the user responds
     * to the permission request for the specified USB device. It then calls the `requestPermission`
     * method of the `UsbManager` to initiate the permission request process.
     *
     * @param device The USB device for which permission is being requested.
     */
    private fun requestPermissionForUsbDevice(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            0,
            Intent(ACTION_USB_DEVICE_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )

        usbManager.requestPermission(device, permissionIntent)
    }


    /**
     * Unregisters the broadcast receiver for USB device events.
     *
     * This method attempts to unregister the `usbDeviceReceiver` that was previously registered
     * to listen for USB device attachment and detachment events. It is important to unregister
     * the receiver to prevent memory leaks and ensure that the application does not continue to
     * receive broadcasts after it is no longer active.
     *
     * If an IllegalArgumentException is thrown (for example, if the receiver was not registered),
     * it catches the exception and logs the error message for debugging purposes.
     */
    private fun unregisterBroadcastReceivers() {
        try {
            unregisterReceiver(usbDeviceReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "onDestroy: ${e.message}")
        }
    }


    /**
     * Checks and requests storage-related permissions based on the device's Android version.
     *
     * This method handles permission requests for accessing storage. It differentiates between
     * devices running Android 11 (API level 30) and above, which require special permissions
     * for managing all files, and devices running earlier versions, which require standard
     * media file access permissions.
     *
     * If the necessary permissions are not granted, it launches the appropriate permission
     * request. If permissions are already granted, it displays a toast message indicating
     * that access is already granted.
     */
    private fun checkAndRequestStorageRelatedPermissions() {
        // If Device build version code is 30 or More
        // Request permission to handle/manage all files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!PermissionUtils.hasAllFileAccessPermission()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${packageName}")
                }
                allFilesStoragePermissionLauncher.launch(intent)
                return
            }
        } else {
            // Else just checking requesting permission to handle media files
            if (!PermissionUtils.hasMediaFileAccessPermissions(this@MainActivity)) {
                mediaFilesStoragePermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
                return
            }
        }

        // If Already granted
        // Do something here
        fetchingConnectedUsbDeviceIfAny()
    }

    /**
     * Requests access to a directory for file operations.
     *
     * This method initiates a request for directory access using an intent. It first attempts to
     * create a specific directory access intent via the `TransferFileManager`. If a compatible
     * file manager is available to handle this intent, it launches the request. If not, it falls
     * back to a more general request using `ACTION_OPEN_DOCUMENT_TREE`.
     *
     * The method is executed within a coroutine scope to handle asynchronous operations. If an
     * exception occurs during the process, it updates the `DownloadAndTransferState` with an error
     * message and the associated file.
     *
     * @param file An optional File object representing the directory for which access is requested.
     */
    @SuppressLint("QueryPermissionsNeeded")
    private fun requestDirectoryAccess(file: File? = null) {
        lifecycleScope.launch {
            try {
                val intent = FileTransferHandler.createDirectoryAccessIntent(this@MainActivity)

                // Check if there's a file manager that can handle our request
                if (intent.resolveActivity(packageManager) != null) {
                    Log.d(TAG, "requestDirectoryAccess: Package manager handling the request")
                    directoryAccessLauncher.launch(intent)
                } else {
                    // If no file manager can handle our specific request, fall back to basic request
                    Log.d(TAG, "requestDirectoryAccess: Document Tree")
                    directoryAccessLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
                }
            } catch (e: Exception) {
                downloadAndTransferViewModel.update(
                    DownloadAndTransferState.TransferError(
                        e.message ?: "Unknown error", file
                    )
                )
            }
        }
    }


    /**
     * Called when the activity is about to be destroyed.
     *
     * This method is a lifecycle callback that allows the activity to perform any final cleanup
     * before it is destroyed. It calls the superclass implementation and unregisters any
     * broadcast receivers that were registered during the activity's lifecycle to prevent memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        _deviceFetchingJob?.let {
            if (it.isActive) it.cancel()
        }
        unregisterBroadcastReceivers()
    }

    /**
     * Companion object for the `MainActivity` class.
     *
     */
    companion object {
        private const val TAG = "MainActivity"
        private const val ACTION_USB_DEVICE_PERMISSION =
            "np.com.naxa.drone_tasking_manager.USB_PERMISSION"
    }
}