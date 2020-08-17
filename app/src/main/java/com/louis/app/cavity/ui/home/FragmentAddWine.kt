package com.louis.app.cavity.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.louis.app.cavity.R
import com.louis.app.cavity.databinding.FragmentAddWineBinding

class FragmentAddWine : Fragment(R.layout.fragment_add_wine) {
    private lateinit var binding: FragmentAddWineBinding
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddWineBinding.bind(view)
    }
}