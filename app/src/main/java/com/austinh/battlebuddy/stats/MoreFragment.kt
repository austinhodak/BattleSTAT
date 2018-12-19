package com.austinh.battlebuddy.stats


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.austinh.battlebuddy.R

class MoreFragment : Fragment() {

    private var mainView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_more, container, false)

        return mainView
    }


}
