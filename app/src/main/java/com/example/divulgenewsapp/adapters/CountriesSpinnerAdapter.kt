package com.example.divulgenewsapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.divulgenewsapp.databinding.CountrySpinnerItemBinding
import com.example.divulgenewsapp.models.Country

class CountriesSpinnerAdapter(
    context: Context, private val items: List<Country>
) : ArrayAdapter<Country>(context, 0, items) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        val country = getItem(position)
        val view = CountrySpinnerItemBinding.inflate(LayoutInflater.from(context))
        view.ivCountryFlagImage.setImageResource(country!!.imageResourceId)
        view.tvCountryName.text = country.country
        view.root.tag = country.value
        return view.root
    }
}