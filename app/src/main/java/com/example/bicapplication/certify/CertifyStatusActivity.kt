package com.example.bicapplication.certify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bicapplication.R
import com.example.bicapplication.databinding.ActivityCertifyStatusBinding

//진행중인 챌린지의 인증현황을 보여주는 액티비티
class CertifyStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCertifyStatusBinding
    private var adapter: CertifyStatusAdapter? = null
    private var certifyDataList: Array<CertifyData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCertifyStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        certifyDataList = arrayOf(
            CertifyData("aa", R.drawable.bic_logo),
            CertifyData("bb",R.drawable.bic_logo),
            CertifyData("cc",R.drawable.bic_logo),
            CertifyData("dd",R.drawable.bic_logo),
            CertifyData("ee",R.drawable.bic_logo)
        )

        adapter = CertifyStatusAdapter(this, certifyDataList)
        binding.gridView.adapter = adapter
    }


}