package com.paulzin.smarthouseandroid.adapter

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paulzin.smarthouseandroid.R
import com.paulzin.smarthouseandroid.model.Device
import kotlinx.android.synthetic.main.item_device.view.*

class DeviceAdapter(val itemClick: (String, Boolean) -> Unit) :
        RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private var items : List<Device>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindDevice(items!![position])
    }

    fun setItems(newItems : List<Device>) {
        val oldItems = items
        items = newItems
        if (oldItems == null) {
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

    override fun getItemCount() = items?.size ?: 0

    class ViewHolder(view: View, val toggle: (String, Boolean) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindDevice(device: Device) {
            with(device) {
                itemView.deviceName.text = name
                itemView.deviceSwitch.isChecked = turnedOn
                itemView.deviceSwitch.setOnCheckedChangeListener { button, value -> toggle(deviceId, value) }
            }
        }
    }
}