package com.summonelec.inspectiongenie.Inspection.PageFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.summonelec.inspectiongenie.Inspection.HomeActivity
import com.summonelec.inspectiongenie.R
import com.summonelec.inspectiongenie.ScannerActivity
import com.summonelec.inspectiongenie.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.scanBarcode.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            intent.putExtra("method", "barcode")
            startActivity(intent)
        })
        binding.manualSearch.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireContext(), ScannerActivity::class.java)
            intent.putExtra("method", "manual")
            startActivity(intent)
        })
        return  root
    }


}