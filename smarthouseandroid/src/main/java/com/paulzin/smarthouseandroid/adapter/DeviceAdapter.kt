package com.paulzin.smarthouseandroid.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paulzin.smarthouseandroid.R
import com.paulzin.smarthouseandroid.model.Device
import kotlinx.android.synthetic.main.item_device.view.*
import java.util.*

class DeviceAdapter(val toggleListener: (String, Boolean) -> Unit,
                    val clickListener: (Device) -> Unit) :
        RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    var deviceList: List<Device> = Collections.emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return ViewHolder(view, toggleListener, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindDevice(deviceList[position])
    }

    fun setItems(newItems : List<Device>) {
        val oldItems = deviceList
        deviceList = newItems
        if (oldItems.isEmpty()) {
            notifyDataSetChanged()
        } else {
            DiffUtil.calculateDiff(object: DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = oldItems[oldItemPosition]
                    val newItem = newItems[newItemPosition]

                    if (oldItem is Device && newItem is Device
                            && oldItem.deviceId === newItem.deviceId) {
                        return true
                    }

                    return false
                }

                override fun getOldListSize(): Int {
                    return oldItems.size
                }

                override fun getNewListSize(): Int {
                    return newItems.size
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = oldItems[oldItemPosition]
                    val newItem = newItems[newItemPosition]

                    return oldItem == newItem
                }
            }, true).dispatchUpdatesTo(this)
        }
    }

    override fun getItemCount() = deviceList.size

    class ViewHolder(view: View,
                     val toggleListener: (String, Boolean) -> Unit,
                     val clickListener: (Device) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindDevice(device: Device) {
            with(itemView) {
                setOnClickListener { _ -> clickListener(device) }
                deviceName.text = device.name
                deviceSwitch.isChecked = device.turnedOn
                deviceSwitch.setOnCheckedChangeListener { _, value -> toggleListener(device.deviceId, value) }
            }
        }
    }
}