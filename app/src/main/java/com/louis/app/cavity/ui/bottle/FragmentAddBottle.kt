package com.louis.app.cavity.ui.bottle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddBottleBinding

class FragmentAddBottle : Fragment(R.layout.fragment_add_bottle) {
    private lateinit var binding: FragmentAddBottleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddBottleBinding.bind(view)
    }
}