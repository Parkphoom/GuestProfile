package com.example.guestprofile

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onShow
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.afollestad.materialdialogs.list.listItems
import com.example.guestprofile.Data.DataHolder
import com.example.guestprofile.Data.DetailInfo
import com.example.guestprofile.Data.Occupation
import com.example.guestprofile.databinding.ActivityDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class DetailActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityDetailBinding
    private val TAG = "DetailActivityLOG"
    var detailInfo: DetailInfo = DetailInfo()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.rr3Btn.setOnClickListener {
            detailInfo.occupation = binding.occupationEdt.text.toString()
            detailInfo.from = binding.fromEdt.text.toString()
            detailInfo.destination = binding.destinationEdt.text.toString()
            detailInfo.roomNo = binding.roomNoEdt.text.toString()
            detailInfo.roomRate = binding.roomRateEdt.text.toString()
            detailInfo.checkinDate = binding.checkinEdt.text.toString()
            detailInfo.checkoutDate = binding.checkoutEdt.text.toString()

            DataHolder.setDetailInfo(detailInfo);
            startActivity(Intent(this@DetailActivity, PdfCreatorExampleActivity::class.java))
        }

        binding.occupationEdt.setOnClickListener(this)
        binding.checkinEdt.setOnClickListener(this)
        binding.checkoutEdt.setOnClickListener(this)

        val edtAR = arrayOf(
            binding.roomNoEdt,
            binding.roomRateEdt,
            binding.checkinEdt,
            binding.checkoutEdt
        )
        ontextchange(
            edtAR
        )

    }

    override fun onClick(v: View?) {
        if (v == binding.occupationEdt) {

            val a = arrayListOf<String>()
            val jsonString = assets.readFile("occupation.json")
            val gson = Gson()
            val arrayTutorialType = object : TypeToken<Array<Occupation>>() {}.type
            val tutorials: Array<Occupation> = gson.fromJson("""[$jsonString]""", arrayTutorialType)
            for (i in tutorials.indices) {
                a.add(tutorials[i].occupation)
            }
            MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                listItems(items = a) { _, index, text ->
                    binding.occupationEdt.setText(text.toString())
                }
                positiveButton(R.string.submit)
                negativeButton(R.string.cancle)
            }
        }
        if (v == binding.checkinEdt) {
            val dialog = AppCompatDialog(this)
            dialog.setCancelable(false)
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.progress_loading)

            runOnUiThread {

                dialog.show()
                MaterialDialog(this).show {
                    onShow {
                        dialog.cancel()
                    }
                    dateTimePicker(requireFutureDateTime = true) { v, dateTime ->
                        val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
                        binding.checkinEdt.setText(format.format(dateTime.time))
                    }
                }
            }
        }
        if (v == binding.checkoutEdt) {
            val dialog = AppCompatDialog(this)
            dialog.setCancelable(false)
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.progress_loading)

            runOnUiThread {

                dialog.show()
                MaterialDialog(this).show {
                    onShow {
                        dialog.cancel()
                    }
                    datePicker { v, dateTime ->
                        val format = SimpleDateFormat("dd/MM/yyyy 12:00")
                        binding.checkoutEdt.setText(format.format(dateTime.time))
                    }
                }
            }
        }

    }

    fun AssetManager.readFile(fileName: String) = open(fileName)
        .bufferedReader()
        .use { it.readText() }

    private fun ontextchange(fields: Array<EditText>) {
        for (i in fields.indices) {
            val currentField = fields[i]
            currentField.doAfterTextChanged {
                binding.rr3Btn.isEnabled = validate(fields)
            }
        }
    }

    private fun validate(fields: Array<EditText>): Boolean {
        for (i in fields.indices) {
            val currentField = fields[i]

            if (currentField.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }

}