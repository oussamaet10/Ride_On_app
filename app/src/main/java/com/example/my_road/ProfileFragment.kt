package com.example.my_road

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap.Config
import android.graphics.Color

class ProfileFragment : Fragment(R.layout.profile_fragment) {

    private lateinit var getQRCodeButton: Button
    private lateinit var driverNameAge: TextView
    private lateinit var profileImage: ImageView

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the views first
        getQRCodeButton = view.findViewById(R.id.getQRCodeButton)
        driverNameAge = view.findViewById(R.id.driverNameAge)
        profileImage = view.findViewById(R.id.profileImage)

        // Load profile image using Glide
        Glide.with(requireContext())
            .load(R.drawable.profile_placeholder)  // Or a URL if it's a network image
            .override(150, 150)  // Resize the image to avoid large bitmaps
            .centerCrop()  // Scale the image and crop it to fit the view
            .into(profileImage)

        // Set the name and age dynamically
        driverNameAge.text = "Oussama ET-TAJANI\n21"  // Update this with actual values

        // On button click, show the QR code
        getQRCodeButton.setOnClickListener {
            showQRCodeDialog()
        }
    }

    private fun showQRCodeDialog() {
        // Create a QR code with driver info (or any relevant information)
        val driverInfo = "Full Name : Oussama ET-TAJANI \n Age : 21 \n Taxi License: XYZ123 \n Type of DL : B \n Taxi's Plate N : 234154-A-33 \n Work Since : 2012"  // Update with actual info
        val qrCodeBitmap = generateQRCode(driverInfo)

        // Create and show the AlertDialog
        val builder = AlertDialog.Builder(requireContext())
        val qrCodeImageView = ImageView(context)
        qrCodeImageView.setImageBitmap(qrCodeBitmap)

        builder.setTitle("Driver QR Code")
            .setView(qrCodeImageView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun generateQRCode(content: String): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
