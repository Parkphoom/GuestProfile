package com.example.guestprofile

import amlib.ccid.Reader
import amlib.ccid.ReaderException
import amlib.ccid.SCError
import amlib.hw.HWType
import amlib.hw.HardwareInterface
import amlib.hw.ReaderHwException
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.*
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.example.guestprofile.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wacinfo.wacextrathaiid.Data.CardAddress
import com.wacinfo.wacextrathaiid.Data.CardPhoto
import com.wacinfo.wacextrathaiid.Data.NativeCardInfo
import com.wacinfo.wacextrathaiid.Data.PostalCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.experimental.or

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "MainActivityLOG"

    //pcsc
    private var mReader: Reader? = null
    private var mMyDev: HardwareInterface? = null
    private var mUsbDev: UsbDevice? = null
    private var mManager: UsbManager? = null
    var mSlotDialog: AlertDialog.Builder? = null
    var nativeCardInfo: NativeCardInfo? = NativeCardInfo()
    var cardPhoto: CardPhoto? = CardPhoto()

    //Builder  mPowerDialog;
    private var mSlotNum: Byte = 0
    private var mPermissionIntent: PendingIntent? = null
    private var mReaderAdapter: ArrayAdapter<String>? = null
    private var mCloseProgress: ProgressDialog? = null
    private var mReaderSpinner: Spinner? = null
    private var mStrMessage: String? = null
    var mContext: Context? = null

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "Alcor-Test"
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        const val MULTIPLE_PERMISSIONS = 100
        private val hexArray = "0123456789ABCDEF".toCharArray()

        val titlenameList =  listOf<String>(
            "นางสาว",
            "นาง",
            "นาย",
            "เด็กหญิง",
            "เด็กชาย",
            "พระสงฆ์",
            "บาทหลวง",
            "หม่อมหลวง",
            "หม่อมราชวงศ์",
            "หม่อมเจ้า",
            "ศาสตราจารย์เกียรติคุณ (กิตติคุณ)",
            "ศาสตราจารย์",
            "ผู้ช่วยศาสตราจารย์",
            "รองศาสตราจารย์"
        )
        val genderList =  listOf<String>(
            "ชาย",
            "หญิง",
            "อื่นๆ"
        )
        val nationList =  listOf<String>(
            "ชาย",
            "หญิง",
            "อื่นๆ"
        )

        private fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF // Here is the conversion
                hexChars[j * 2] = hexArray[v.ushr(4)]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }

        private fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        private fun formatString(raw: String): String {
            var str_format = raw.replace(" ".toRegex(), "")
            str_format = str_format.replace("\n".toRegex(), "")
            str_format = str_format.replace("\u0090".toRegex(), "")
            str_format = str_format.replace("##".toRegex(), " ")
            str_format = str_format.replace("#".toRegex(), " ")
            str_format = str_format.replace("\\s".toRegex(), " ")
            str_format = str_format.replace("\\0", "").replace("\u0000", "")

            return str_format
        }

    }


    fun <P, R> CoroutineScope.executeAsyncTask(
        onPreExecute: () -> Unit,
        doInBackground: suspend (suspend (P) -> Unit) -> R,
        onPostExecute: (R) -> Unit,
        onProgressUpdate: (P) -> Unit
    ) = launch {
        onPreExecute()

        val result = withContext(Dispatchers.IO) {
            doInBackground {
                withContext(Dispatchers.Main) { onProgressUpdate(it) }
            }
        }
        onPostExecute(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


        setupViews()
        // Start USB interface
        mSlotNum = 0.toByte()
        mContext = applicationContext
        try {
            mMyDev = HardwareInterface(HWType.eUSB, this.applicationContext)
            //mMyDev.setLog(mContext,true, 0xff);
        } catch (e: Exception) {
            mStrMessage = "Get Exception : " + e.message
            Log.e(TAG, mStrMessage!!)
            (TAG + " :: " + mStrMessage!!)

            return
        }
        // Get USB manager
        Log.d(TAG, " mManager")
        (TAG + " :: " + " mManager")
        mManager = getSystemService(USB_SERVICE) as UsbManager
        findDevice()

    }

    // Disconnect USB device when exit app
    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        if (mMyDev != null) mMyDev!!.Close()
        super.onDestroy()
    }

    fun setupViews() {
        supportActionBar?.hide()
        binding.readBtn.setOnClickListener(this)
        binding.startBtn.setOnClickListener(this)
        binding.refreshBtn.setOnClickListener(this)
        binding.titleEdt.setOnClickListener(this)
        binding.genderEdt.setOnClickListener(this)
        binding.nationalityEdt.setOnClickListener(this)
        setupReaderSpinner()
        setReaderSlotView()
    }

    //read zipcode.json in assets folder
    fun AssetManager.readFile(fileName: String) = open(fileName)
        .bufferedReader()
        .use { it.readText() }

    //set usb reader to list item
    private fun setReaderSlotView() {
        val arraySlot = arrayOf("slot:0", "Slot:1")
        mSlotDialog = AlertDialog.Builder(this)
        val Select = DialogInterface.OnClickListener { dialog, which -> mSlotNum = which.toByte() }
        val OkClick = DialogInterface.OnClickListener { dialog, which -> requestDevPerm() }
        mSlotDialog!!.setPositiveButton("OK", OkClick)
        mSlotDialog!!.setTitle("Select Slot Number")
        mSlotDialog!!.setSingleChoiceItems(arraySlot, 0, Select)

        val dev = getSpinnerSelect()
        if (dev != null) {
            checkSlotNumber(dev)
        }
    }

    private fun findDevice() {
        toRegisterReceiver()
        EnumeDev()
    }

    fun openReader() {
        val dev = getSpinnerSelect()
        if (dev != null) {
            checkSlotNumber(dev)
        }
    }

    private fun setupReaderSpinner() {
        // Initialize reader spinner list
        mReaderAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item
        )
        mReaderSpinner = findViewById(R.id.spinnerDevice)
        mReaderSpinner?.adapter = mReaderAdapter
        mReaderSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?,
                arg1: View,
                position: Int,
                arg3: Long
            ) {

            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {

            }
        }
    }

    private fun checkSlotNumber(uDev: UsbDevice) {
        if (uDev.productId == 0x9522 || uDev.productId == 0x9525 ||
            uDev.productId == 0x9526 || uDev.productId == 0x9572
        )
            mSlotDialog!!.show() else {
            mSlotNum = 0.toByte()
            requestDevPerm()
        }
    }

    private fun toRegisterReceiver() {
        // Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        registerReceiver(mReceiver, filter)
    }

    //Get device information and check device is alcorReader?
    private fun EnumeDev(): Int {
        var device: UsbDevice? = null
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        Log.d(TAG, " EnumeDev")
        mReaderAdapter!!.clear()
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next()
            Log.d(
                TAG,
                " " + Integer.toHexString(device.vendorId) + " " + Integer.toHexString(device.productId)
            )
            if (isAlcorReader(device)) {
                Log.d(TAG, "Found Device")
                mReaderAdapter!!.add(device.deviceName)
            }
        }
        requestDevPerm()
        return 0
    }

    //Select usb device on connected and check request permission to use device
    private fun requestDevPerm() {
        val dev = getSpinnerSelect()
        if (dev != null) mManager!!.requestPermission(dev, mPermissionIntent)
        else Log.e(
            TAG,
            "selected not found"
        )
    }

    //Select usb device on connected
    private fun getSpinnerSelect(): UsbDevice? {
        val deviceName: String? = mReaderSpinner!!.selectedItem as? String
        if (deviceName != null) {
            // For each device
            for (device in mManager!!.deviceList.values) {
                if (deviceName == device.deviceName) {

                    return device
                }
            }
        }
        return null
    }

    //ทำงานเช็คสถานะการเชื่อมต่อ reader และเช็คอนุญาตการใช้งาน
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Broadcast Receiver")
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        if (device != null) {
                            onDevPermit(device)
                        } else {

                        }
                    } else {
                        Log.d(TAG, "Permission denied for device " + device!!.deviceName)
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                Log.d(TAG, "Device Detached")
                onDetache(intent)
                synchronized(this) { updateReaderList(intent) }
            }
        } /*end of onReceive(Context context, Intent intent) {*/
    }

    private fun onDevPermit(dev: UsbDevice) {
        mUsbDev = dev
        try {
            updateViewReader()
            OpenTask().execute(dev)
        } catch (e: Exception) {
            mStrMessage = "Get Exception : " + e.message
            Log.e(TAG, mStrMessage!!)
        }
    }

    private fun onDetache(intent: Intent) {
        binding.readBtn.isEnabled = false
        val udev = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
        if (udev != null) {
            if (udev == mUsbDev) {
                closeReaderUp()
                closeReaderBottom()
            }
        } else {
            Log.d(TAG, "usb device is null")
        }
    }


    private fun updateReaderList(intent: Intent) {
        // Update reader list
        mReaderAdapter!!.clear()
        for (device in mManager!!.deviceList.values) {
            Log.d(TAG, "Update reader list : " + device.deviceName)
            if (isAlcorReader(device)) mReaderAdapter!!.add(device.deviceName)
        }
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

    }

    private fun updateViewReader() {
        //int pid = 0;
        //int vid = 0;
        try {
            //pid = mUsbDev.getProductId();
            //vid = mUsbDev.getVendorId();
            mStrMessage = "updateViewReader"
            Log.e(TAG, mStrMessage!!)
            (" updateViewReader  :: $mStrMessage")
        } catch (e: NullPointerException) {
            mStrMessage = "Get Exception : " + e.message
            Log.e(TAG, mStrMessage!!)
            ("mStrupdateView Get Exception :: $e.message")
            return
        }
    }

    //    ตัวจัดการเปิดการทำงาน USB reader
    private inner class OpenTask : AsyncTask<UsbDevice?, Void?, Int>() {
        override fun doInBackground(vararg p0: UsbDevice?): Int? {
            var status = 0
            try {
                status = InitReader()
                if (status != 0) {
                    ("fail to initial reader")
                    Log.e(TAG, "fail to initial reader")
                    return status
                }
                //status = mReader.connect();
            } catch (e: Exception) {
                mStrMessage = "Get Exception : " + e.message
                ("Open fail :: " + "Get Exception : " + e.message)
            }
            return status
        }

        override fun onPostExecute(result: Int) {
            if (result != 0) {
                Log.e(TAG, "Open fail: " + Integer.toString(result))
                Toast.makeText(
                    this@MainActivity,
                    "Open fail: " + Integer.toString(result),
                    Toast.LENGTH_SHORT
                ).show()
                ("Open fail dataTop:: $result ::")
            } else {
                onOpenButtonSetup()
                Log.e(TAG, "Open successfully")
                PersonalInfoTextViewClear()
            }
        }


    }

    //ปิดการใช้งานเครื่องอ่านบัตร
    private fun closeReaderUp(): Int {
        Log.d(TAG, "Closing reader...")
        var ret = 0
        if (mReader != null) {
            ret = mReader!!.close()
        }
        return ret
    }

    //ปิดการเชื่อมต่อ USB
    private fun closeReaderBottom() {
        onCloseButtonSetup()
        mMyDev!!.Close()
        mSlotNum = 0.toByte()
    }

    //    สั่งเปิดใช้งาน USB reader ที่เชื่อมต่อและผ่านการอนุญาตการใช้งานแล้ว
    private fun InitReader(): Int {
        var Status = 0
        val init: Boolean //
        Log.d(TAG, "InitReader")
        try {
            init = mMyDev?.Init(mManager, mUsbDev)!!

            if (!init) {
                Log.e(TAG, "Device init fail")
                return -1
            }
        } catch (e: ReaderHwException) {
            Log.e(
                TAG,
                "Get ReaderHwException : " + e.message
            )
            return -1
        }
        try {
            mReader = Reader(mMyDev)
            Status = mReader!!.open()
        } catch (e: ReaderException) {
            Log.e(
                TAG,
                "InitReader fail " + "Get Exception : " + e.message
            )
            return -1
        }
        mReader!!.setSlot(mSlotNum)
        return Status
    }

    //Check USB device is AlcorReader
    private fun isAlcorReader(udev: UsbDevice?): Boolean {
        if (udev!!.vendorId == 0x058f
            && (udev.productId == 0x9540
                    || udev.productId == 0x9520 || udev.productId == 0x9522
                    || udev.productId == 0x9525 || udev.productId == 0x9526)
        ) return true else if (udev.vendorId == 0x2CE3
            && (udev.productId == 0x9571 || udev.productId == 0x9572
                    || udev.productId == 0x9563) || udev.productId == 0x9573
        ) {
            return true
        }
        return false
    }


    private fun onOpenButtonSetup() {
        binding.startBtn!!.isEnabled = false
        binding.readBtn!!.isEnabled = true
    }

    private fun onCloseButtonSetup() {
        binding.startBtn!!.isEnabled = true
        binding.readBtn!!.isEnabled = false
    }

    //Clear view layout
    private fun PersonalInfoTextViewClear() {

        runOnUiThread {
            binding.imageCard.setImageResource(R.drawable.ic_baseline_person_24)
            //imgPhoto.setImageDrawable(null);

            binding.tmNoEdt.text.clear()
            binding.passportEdt.text.clear()
            binding.personEdt.text.clear()
            binding.titleEdt.text.clear()
            binding.firstnameEdt.text.clear()
            binding.middleEdt.text.clear()
            binding.lastnameEdt.text.clear()
            binding.genderEdt.text.clear()
            binding.dobEdt.text.clear()
            binding.doeEdt.text.clear()
            binding.nationalityEdt.text.clear()
            binding.addressEdt.text.clear()

        }

    }

    override fun onClick(v: View?) {
        if (v == binding.readBtn) {
            val dialog = ProgressDialog(this)
            dialog.setTitle(R.string.reading)
            dialog.setMessage(getString(R.string.do_not_eject))
            lifecycleScope.executeAsyncTask(
                onPreExecute = {
                    // ... runs in Main Thread
                    dialog.show()
                }, doInBackground = { publishProgress: suspend (progress: Int) -> Unit ->
                    ReadCard()
                    "Result" // send data to "onPostExecute"
                }, onPostExecute = {
                    dialog.dismiss()
                    // runs in Main Thread
                    // ... here "it" is a data returned from "doInBackground"
                }, onProgressUpdate = {

                    // runs in Main Thread
                    // ... here "it" contains progress
                }
            )
        }
        if (v == binding.startBtn) {
            openReader()
        }
        if (v == binding.refreshBtn) {
            findDevice()
            nativeCardInfo = NativeCardInfo()
            nativeCardInfo!!.address = CardAddress()
        }
        if (v == binding.titleEdt) {

            MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                listItems(items = titlenameList) { _, index, text ->
                    binding.titleEdt.setText(text.toString())
                }
                positiveButton(R.string.submit)
                negativeButton(R.string.cancle)
            }
        }
        if (v == binding.genderEdt) {

            MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                listItems(items = genderList) { _, index, text ->
                    binding.genderEdt.setText(text.toString())
                }
                positiveButton(R.string.submit)
                negativeButton(R.string.cancle)
            }
        }
        if (v == binding.nationalityEdt) {

            MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                listItems(items = nationList) { _, index, text ->
                    binding.nationalityEdt.setText(text.toString())
                }
                positiveButton(R.string.submit)
                negativeButton(R.string.cancle)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    fun ReadCard() {

        PersonalInfoTextViewClear()

        //****  SET POWER ON ****//
        val ret: Int
        ret = poweron()
        if (ret == SCError.READER_SUCCESSFUL) {
            val atr: String
            try {
                atr = mReader!!.atrString
                //mTextViewResult.setText(" ATR:"+ atr);
            } catch (e: Exception) {
                mStrMessage = "Get Exception : " + e.message
            }
            var recByte: ByteArray? = null

            recByte = SendAPDUcommand("00A4040008A000000054480001") //SELECT COMMAND
            val arrayInfo: Array<String>

            recByte = SendAPDUcommand("80B0000402000D") //CID
            recByte = SendAPDUcommand("00C000000D") //CID

            var str_CID: String = ""
            try {
                str_CID = String(recByte!!, charset("TIS620"))
                str_CID = str_CID.substring(0, 13)
                runOnUiThread {
                    binding.personEdt.setText(str_CID)
                }

                nativeCardInfo!!.cardNumber = str_CID
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            recByte = SendAPDUcommand("80B00011020064") //FULL NANE TH
            recByte = SendAPDUcommand("00C0000064") //FULL NANE TH
            var fn_th: Array<String>? = null
            try {
                fn_th =
                    String(recByte!!, charset("TIS620")).substring(0, 35).split("#").toTypedArray()
                var tilteNameTh = fn_th[0] // คำนำหน้า
                var firstNameTh = fn_th[1] // ชื่อ
                var middleNameTh = fn_th[2].replace("", "-") // ว่าง
                var lastNameTh = fn_th[3].replaceFirst(" ", "") // last name

//                binding.titleEdt!!.text = "$tilteNameTh $firstNameTh $middleNameTh $lastNameTh"
                runOnUiThread {
                    binding.titleEdt.setText(tilteNameTh)
                    binding.firstnameEdt.setText(firstNameTh)
                    binding.middleEdt.setText(middleNameTh)
                    binding.lastnameEdt.setText(lastNameTh)
                }


                nativeCardInfo!!.thaiTitle = tilteNameTh
                nativeCardInfo!!.thaiFirstName = firstNameTh
                nativeCardInfo!!.thaiMiddleName = middleNameTh
                nativeCardInfo!!.thaiLastName = lastNameTh


            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            recByte = SendAPDUcommand("80B000D9020008") //DOB
            recByte = SendAPDUcommand("00C0000008") //DOB
            var str_DOB: String? = null
            try {
                str_DOB = String(recByte!!, charset("TIS620"))
                str_DOB = str_DOB.substring(
                    0,
                    8
                )
                str_DOB =
                    str_DOB.substring(6, 8) + "/" + str_DOB.substring(
                        4,
                        6
                    ) + "/" + str_DOB.substring(
                        0,
                        4
                    )
                runOnUiThread {
                    binding.doeEdt.setText(str_DOB)
                }

                nativeCardInfo!!.dateOfBirth = str_DOB
            } catch (e: UnsupportedEncodingException) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }

            recByte = SendAPDUcommand("80B000E1020001") //GENDER
            recByte = SendAPDUcommand("00C0000001") //GENDER
            var str_Gender: String? = null
            try {
                str_Gender = String(recByte!!, charset("TIS620"))
                val b = str_Gender.startsWith("1")
                str_Gender = if (b == true) {
                    "ชาย"
                } else {
                    "หญิง"
                }
                runOnUiThread {
                    binding.genderEdt.setText(str_Gender)
                }

                nativeCardInfo!!.sex = str_Gender
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            recByte = SendAPDUcommand("80B00167020012") //ISSUE/EXPIRE
            recByte = SendAPDUcommand("00C0000012") //ISSUE/EXPIRE

            var str_ISSUEEXPIRE: String? = null
            try {
                str_ISSUEEXPIRE = String(recByte!!, charset("TIS620"))
                var strIssue = str_ISSUEEXPIRE.substring(0, 8)
                var strExpire = str_ISSUEEXPIRE.substring(8, 16)
                strIssue = strIssue.substring(6, 8) + "/" + strIssue.substring(
                    4,
                    6
                ) + "/" + strIssue.substring(0, 4)
//                txtIssue!!.text = "$strIssue"
                nativeCardInfo!!.cardIssueDate = strIssue

                strExpire = strExpire.substring(6, 8) + "/" + strExpire.substring(
                    4,
                    6
                ) + "/" + strExpire.substring(0, 4)
//                txtExpire!!.text = "$strExpire"
                runOnUiThread {
                    binding.dobEdt.setText(strExpire)
                }

                nativeCardInfo!!.cardExpiryDate = strExpire
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }


            recByte = SendAPDUcommand("80B01579020064") //Address
            recByte = SendAPDUcommand("00C0000064") //Address
            var strAddress: Array<String>? = null
            var Address: String = ""
            try {
                Address = String(recByte!!, charset("TIS620"))
                Address = Address.replace("#", " ")
                Address = Address.replace("\\s+".toRegex(), " ")
                Address = Address.replace("\\0", "").replace("\u0000", "").replace(" \u0090", "")
                strAddress = String(recByte, charset("TIS620")).split("#").toTypedArray()
                val enpart12: String = strAddress[0] // บ้านเลขที่

                val enpart13: String = strAddress.get(1) // หมู่ที่

                val enpart14: String = strAddress.get(2)
                val enpart15: String = strAddress.get(3)
                val enpart16: String = strAddress.get(4)
                val enpart17: String = strAddress[5] // ตำบล

                val enpart18: String = strAddress.get(6) // อำเภอ

                val enpart19: String = formatString(strAddress.get(7))// จังหวัด

                val zipcode = findPostalCode(
                    enpart19,
                    enpart18
                )

                runOnUiThread {
                    binding.addressEdt.setText("$enpart12 $enpart13 $enpart14 $enpart15 $enpart16 $enpart17 $enpart18 $enpart19 $zipcode")
                }


                var address = CardAddress()
                address.homeNo = enpart12
                address.moo = enpart13
                address.trok = enpart14
                address.soi = enpart15
                address.road = enpart16
                address.subDistrict = enpart17
                address.district = enpart18
                address.province = enpart19
                address.postalCode = zipcode
                address.country = "ประเทศไทย"


                nativeCardInfo!!.address = address
                nativeCardInfo!!.cardCountry = address.country


            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            recByte = SendAPDUcommand("80b0161902000e") //Image code
            recByte = SendAPDUcommand("00c000000e") //Image code
            var str_ImgCode: String? = null
            try {
                str_ImgCode = String(recByte!!, charset("TIS620"))
//                txtImgcode!!.text = "${formatString(str_ImgCode)}"
                nativeCardInfo!!.cardPhotoIssueNo = formatString(str_ImgCode)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            recByte = SendAPDUcommand("80B00000020004") //Version
            recByte = SendAPDUcommand("00C0000004") //Version
            var str_Version: String? = null
            try {
                str_Version = String(recByte!!, charset("TIS620"))
//                txtVersioncard!!.text =
//                    "${formatString(str_Version)}"
//                nativeCardInfo!!.versionCard = txtVersioncard!!.text.toString()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()

            }

            nativeCardInfo!!.statusCode = "1"
            nativeCardInfo!!.cardIssueNo = "null"

            ReadPicClick()


        } else if (ret == SCError.READER_NO_CARD) {
            runOnUiThread {
                Toast.makeText(mContext, "Card Absent", Toast.LENGTH_SHORT).show()
            }

            nativeCardInfo!!.statusCode = "0"
            nativeCardInfo!!.cardIssueNo = "null"

            poweroff()

        } else {
            runOnUiThread {
                Toast.makeText(mContext, "Power on fail", Toast.LENGTH_SHORT).show()
            }
            nativeCardInfo!!.statusCode = "0"
            nativeCardInfo!!.cardIssueNo = "null"

            poweroff()

        }


    }

    fun ReadPicClick() {
        val ret: Int
        ret = poweron()
        if (ret == SCError.READER_SUCCESSFUL) {

        } else if (ret == SCError.READER_NO_CARD) {
            runOnUiThread {
                Toast.makeText(mContext, "Card Absent", Toast.LENGTH_SHORT).show()
            }
            nativeCardInfo!!.statusCode = "0"
            nativeCardInfo!!.cardIssueNo = "null"

            poweroff()

        } else {
            runOnUiThread {
                Toast.makeText(mContext, "Power on fail", Toast.LENGTH_SHORT).show()
            }
            nativeCardInfo!!.statusCode = "0"
            nativeCardInfo!!.cardIssueNo = "null"

            poweroff()

        }
        var recByte: ByteArray? = null
        recByte = SendAPDUcommand("00A4040008A000000054480001") //SELECT COMMAND

        //#Photo
        val pRevAPDULen = IntArray(1)
        var COMMAND: ByteArray
        var recvBuffer = ByteArray(300)
        var hexstring = ""
        var tmp: String
        var r: Int
        r = 0
        pRevAPDULen[0] = 300
        while (r <= 20) {
            COMMAND = byteArrayOf(
                0x80.toByte(),
                0xB0.toByte(),
                (0x01.toByte() + r).toByte(),
                (0x7B.toByte() - r).toByte(),
                0x02.toByte(),
                0x00.toByte(),
                0xFF.toByte()
            )
            recvBuffer = ByteArray(2)
            mReader!!.transmit(COMMAND, COMMAND.size, recvBuffer, pRevAPDULen)

            COMMAND = byteArrayOf(
                0x00.toByte(),
                0xC0.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0xFF.toByte()
            )
            recvBuffer = ByteArray(257)
            mReader!!.transmit(COMMAND, COMMAND.size, recvBuffer, pRevAPDULen)
            val recvBufferClone = Arrays.copyOfRange(recvBuffer, 0, 255)
            tmp = bytesToHex(recvBufferClone)
            hexstring = hexstring + tmp
            r++
        }
        val byteRawHex = hexStringToByteArray(hexstring)
        val imgBase64String = Base64.encodeToString(byteRawHex, Base64.NO_WRAP)
        val bitmapCard = BitmapFactory.decodeByteArray(byteRawHex, 0, byteRawHex.size)
        cardPhoto!!.statusCode = "1"
        cardPhoto!!.photo = imgBase64String
        runOnUiThread {
            binding.imageCard.setImageBitmap(bitmapCard)
        }
//        var cardimage: MultipartBody.Part? = null
//
//        val cardfile = File(this.cacheDir, "image")
//        cardfile.createNewFile()
//        val bos = ByteArrayOutputStream()
//        bitmapCard.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
//        val bitmapdata = bos.toByteArray()
//        val fos = FileOutputStream(cardfile)
//        fos.write(bitmapdata)
//        fos.flush()
//        fos.close()
//
//        val requestcardFile: RequestBody = cardfile.asRequestBody("image/png".toMediaTypeOrNull())
//        cardimage = MultipartBody.Part.createFormData(
//            "image1",
//            cardfile.name,
//            requestcardFile
//        )
//
//        /**set information to data Class before upload*/
//        val uploaddata = Upload.Data()
//        uploaddata.statusCode = nativeCardInfo!!.statusCode
//        uploaddata.smartDataImage = cardimage
//        uploaddata.chipId = nativeCardInfo!!.chipId
//        uploaddata.laserId = nativeCardInfo!!.laserId
//        uploaddata.bp1No = nativeCardInfo!!.bp1No
//        uploaddata.cardNumber = nativeCardInfo!!.cardNumber
//        uploaddata.thaiTitle = nativeCardInfo!!.thaiTitle
//        uploaddata.thaiFirstName = nativeCardInfo!!.thaiFirstName
//        uploaddata.thaiMiddleName = nativeCardInfo!!.thaiMiddleName
//        uploaddata.thaiLastName = nativeCardInfo!!.thaiLastName
//        uploaddata.engTitle = nativeCardInfo!!.engTitle
//        uploaddata.engFirstName = nativeCardInfo!!.engFirstName
//        uploaddata.engMiddleName = nativeCardInfo!!.engMiddleName
//        uploaddata.engLastName = nativeCardInfo!!.engLastName
//        uploaddata.dateOfBirth = nativeCardInfo!!.dateOfBirth
//        uploaddata.sex = nativeCardInfo!!.sex
//        uploaddata.cardPhotoIssueNo = nativeCardInfo!!.cardPhotoIssueNo
//        uploaddata.cardIssuePlace = nativeCardInfo!!.cardIssuePlace
////                            uploaddata.cardIssuerNo = nativeCardInfo!!.cardIssuerNo
//        uploaddata.cardIssuerNo = ""
//        uploaddata.cardIssueNo = nativeCardInfo!!.cardIssueNo
//        uploaddata.cardIssueDate = nativeCardInfo!!.cardIssueDate
//        uploaddata.cardExpiryDate = nativeCardInfo!!.cardExpiryDate
//        uploaddata.cardType = nativeCardInfo!!.cardType
//        uploaddata.versionCard = nativeCardInfo!!.versionCard
//
//
//        val addressdata = Upload.Data.CardAddress()
//        addressdata.homeNo = nativeCardInfo!!.address?.homeNo.toString()
//        addressdata.soi = nativeCardInfo!!.address?.soi.toString()
//        addressdata.trok = nativeCardInfo!!.address?.trok.toString()
//        addressdata.moo = nativeCardInfo!!.address?.moo.toString()
//        addressdata.road = nativeCardInfo!!.address?.road.toString()
//        addressdata.subDistrict =
//            nativeCardInfo!!.address?.subDistrict.toString()
//        addressdata.district = nativeCardInfo!!.address?.district.toString()
//        addressdata.province = nativeCardInfo!!.address?.province.toString()
//        addressdata.postalCode = nativeCardInfo!!.address?.postalCode.toString()
//        addressdata.country = nativeCardInfo!!.address?.country.toString()
//
//        uploaddata.address = addressdata
//        uploaddata.cardCountry = nativeCardInfo!!.cardCountry
//
//        val photodata = Upload.Data.CardPhoto()
//        photodata.statusCode = cardPhoto!!.statusCode
//        photodata.photo = cardPhoto!!.photo
//
//        uploaddata.photo = photodata
//
//        /**create data for upload*/
//        val builder: MultipartBody.Builder =
//            MultipartBody.Builder().setType(MultipartBody.FORM)
//        builder.addFormDataPart("mId", AppSettings.USER_ID)
//            .addFormDataPart("uId", AppSettings.UID)
//            .addFormDataPart(
//                "smartDataImage",
//                "image",
//                requestcardFile
//            )
//            .addFormDataPart("bp1No", nativeCardInfo!!.bp1No)
//            .addFormDataPart("chipId",  nativeCardInfo!!.chipId)
//            .addFormDataPart("cardNumber",  nativeCardInfo!!.cardNumber)
//            .addFormDataPart("thaiTitle",  nativeCardInfo!!.thaiTitle)
//            .addFormDataPart("thaiFirstName", nativeCardInfo!!.thaiFirstName)
//            .addFormDataPart("thaiMiddleName", nativeCardInfo!!.thaiMiddleName)
//            .addFormDataPart("thaiLastName", nativeCardInfo!!.thaiLastName)
//            .addFormDataPart("engTitle", nativeCardInfo!!.engTitle)
//            .addFormDataPart("engFirstName", nativeCardInfo!!.engFirstName)
//            .addFormDataPart("engMiddleName", nativeCardInfo!!.engMiddleName)
//            .addFormDataPart("engLastName", nativeCardInfo!!.engLastName)
//            .addFormDataPart("dateOfBirth", nativeCardInfo!!.dateOfBirth)
//            .addFormDataPart("sex", nativeCardInfo!!.sex)
//            .addFormDataPart("cardIssueNo", nativeCardInfo!!.cardIssueNo)
//            .addFormDataPart("cardIssuePlace", nativeCardInfo!!.cardIssuePlace)
//            .addFormDataPart("cardIssueDate", nativeCardInfo!!.cardIssueDate)
//            .addFormDataPart("cardPhotoIssueNo", nativeCardInfo!!.cardPhotoIssueNo)
//            .addFormDataPart("laserId", nativeCardInfo!!.laserId)
//            .addFormDataPart("cardExpiryDate", nativeCardInfo!!.cardExpiryDate)
//            .addFormDataPart("cardType", nativeCardInfo!!.cardType)
//            .addFormDataPart("homeNo", nativeCardInfo!!.address!!.homeNo)
//            .addFormDataPart("soi", nativeCardInfo!!.address!!.soi)
//            .addFormDataPart("trok", nativeCardInfo!!.address!!.trok)
//            .addFormDataPart("moo", nativeCardInfo!!.address!!.moo)
//            .addFormDataPart("road", nativeCardInfo!!.address!!.road)
//            .addFormDataPart("subDistrict", nativeCardInfo!!.address!!.subDistrict)
//            .addFormDataPart("district", nativeCardInfo!!.address!!.district)
//            .addFormDataPart("province", nativeCardInfo!!.address!!.province)
//            .addFormDataPart("postalCode", nativeCardInfo!!.address!!.postalCode)
//            .addFormDataPart("country", nativeCardInfo!!.address!!.country)
//            .addFormDataPart("cardCountry", nativeCardInfo!!.cardCountry)
//
//
//        //Upload
//        val requestBody: RequestBody = builder.build()
//        uploadReader(requestBody)
//
//
//        onSuccess()
    }

    //จ่ายไฟเข้าบัตรก่อนทำการอ่านบัตร จะเป็นการเช็คว่าบัตรถูกเสียบอย่างถูกต้อง พร้อมอ่านหรือไม่
    private fun poweron(): Int {
        var result = SCError.READER_SUCCESSFUL
        //check slot status first
        result = getSlotStatus()
        when (result) {
            SCError.READER_NO_CARD -> {
                Log.d(TAG, "Card Absent")
                return SCError.READER_NO_CARD
            }
            SCError.READER_CARD_INACTIVE, SCError.READER_SUCCESSFUL -> {
            }
            else -> return result
        }
        result = mReader!!.setPower(Reader.CCID_POWERON)
        Log.d(TAG, "power on exit")
        return result
    }

    //ปิดการจ่ายไฟเข้าบัตรก่อนทำการอ่านบัตร
    private fun poweroff(): Int {
        var result = SCError.READER_SUCCESSFUL
        Log.d(TAG, "poweroff")
        result = getSlotStatus()
        when (result) {
            SCError.READER_NO_CARD -> {
                Log.d(TAG, "Card Absent")
                return SCError.READER_NO_CARD
            }
            SCError.READER_CARD_INACTIVE, SCError.READER_SUCCESSFUL -> {
            }
            else -> return result
        }
        //----------poweroff card------------------
        result = mReader!!.setPower(Reader.CCID_POWEROFF)
        return result
    }

    fun getSlotStatus(): Int {
        var ret = SCError.READER_NO_CARD
        val pCardStatus = ByteArray(1)

        /*detect card hotplug events*/
        ret = mReader!!.getCardStatus(pCardStatus)
        if (ret == SCError.READER_SUCCESSFUL) {
            ret = if (pCardStatus[0] == Reader.SLOT_STATUS_CARD_ABSENT) {
                SCError.READER_NO_CARD
            } else if (pCardStatus[0] == Reader.SLOT_STATUS_CARD_INACTIVE) {
                SCError.READER_CARD_INACTIVE
            } else {
                SCError.READER_SUCCESSFUL
            }
        }
        return ret
    }

    private fun SendAPDUcommand(strAPDU: String): ByteArray? {
        val pSendAPDU: ByteArray
        val pRecvRes = ByteArray(300)
        val pRevAPDULen = IntArray(1)
        val apduStr: String
        val sendLen: Int
        val result: Int
        pRevAPDULen[0] = 300
        apduStr = strAPDU.trim { it <= ' ' }
        pSendAPDU = toByteArray(apduStr)
        sendLen = pSendAPDU.size
        return try {
            result = mReader!!.transmit(pSendAPDU, sendLen, pRecvRes, pRevAPDULen)
            if (result == SCError.READER_SUCCESSFUL) {
                //mTextViewResult.setText("Receive APDU: "+ logBuffer(pRecvRes, pRevAPDULen[0]));
                pRecvRes
            } else {
                //                mTextViewResult.setText("Fail to Send APDU: " + Integer.toString(result)
                //                        + "("+ Integer.toHexString(mReader.getCmdFailCode()) +")");
                Log.e(
                    TAG, "Fail to Send APDU: " + Integer.toString(result)
                            + "(" + Integer.toHexString(mReader!!.cmdFailCode) + ")"
                )
                null
            }
        } catch (e: Exception) {
            mStrMessage = "Get Exception : " + e.message
            Log.e(TAG, mStrMessage!!)
            null
        }
    }

    private fun findPostalCode(province: String, district: String): String {
        val jsonString = assets.readFile("zipcode.json")
        val gson = Gson()
        val arrayTutorialType = object : TypeToken<Array<PostalCode>>() {}.type
        val tutorials: Array<PostalCode> = gson.fromJson("""[$jsonString]""", arrayTutorialType)
        val findlist = (tutorials.filter {
            it.province == province.replace(
                "จังหวัด",
                ""
            ) && it.district == district.replace(
                "อำเภอ",
                ""
            )
        })
        var zipCode = ""
        for (i in findlist.indices) {
            zipCode = findlist[i].zip
        }
        return zipCode
    }

    private fun toByteArray(hexString: String): ByteArray {
        val hexStringLength = hexString.length
        var byteArray: ByteArray? = null
        var count = 0
        var c: Char
        var i: Int

        // Count number of hex characters
        i = 0
        while (i < hexStringLength) {
            c = hexString[i]
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || (c >= 'a'
                        && c <= 'f')
            ) {
                count++
            }
            i++
        }
        byteArray = ByteArray((count + 1) / 2)
        var first = true
        var len = 0
        var value: Int
        i = 0
        while (i < hexStringLength) {
            c = hexString[i]
            value = if (c >= '0' && c <= '9') {
                c - '0'
            } else if (c >= 'A' && c <= 'F') {
                c - 'A' + 10
            } else if (c >= 'a' && c <= 'f') {
                c - 'a' + 10
            } else {
                -1
            }
            if (value >= 0) {
                if (first) {
                    byteArray[len] = (value shl 4).toByte()
                } else {
                    byteArray[len] = byteArray[len] or value.toByte()
                    len++
                }
                first = !first
            }
            i++
        }
        return byteArray
    }
}