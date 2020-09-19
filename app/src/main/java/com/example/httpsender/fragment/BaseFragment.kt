package com.example.httpsender.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * User: ljx
 * Date: 2020/6/2
 * Time: 11:45
 */
abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    @LayoutRes
    private var layoutId = 0
    protected lateinit var mBinding: T

    fun setContentView(@LayoutRes layoutId: Int) {
        this.layoutId = layoutId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.onViewCreated(savedInstanceState)
    }

    open fun T.onViewCreated(savedInstanceState: Bundle?) {

    }
}