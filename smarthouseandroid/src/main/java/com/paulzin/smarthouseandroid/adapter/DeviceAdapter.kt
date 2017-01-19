package com.paulzin.smarthouseandroid.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.paulzin.smarthouseandroid.R
import com.paulzin.smarthouseandroid.model.Device
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceAdapter(val deviceList: List<Device>, val itemClick: (String, Boolean) -> Unit) :
        RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindDevice(deviceList[position])
    }

    override fun getItemCount() = deviceList.size

    class ViewHolder(view: View, val itemClick: (String, Boolean) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindDevice(device: Device) {
            with(device) {
                itemView.deviceName.text = name
                itemView.deviceSwitch.isChecked = turnedOn
                itemView.deviceSwitch.setOnCheckedChangeListener { button, value -> itemClick(deviceId, value) }
                Glide.with(itemView.context).load(imageUrl).into(itemView.deviceImageView)
            }
        }
    }
}