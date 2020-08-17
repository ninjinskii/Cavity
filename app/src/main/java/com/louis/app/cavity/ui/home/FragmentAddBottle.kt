package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddBottleBinding

class FragmentAddBottle : Fragment(R.layout.fragment_add_bottle) {
    private lateinit var binding: FragmentAddBottleBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddBottleBinding.bind(view)
    }
}