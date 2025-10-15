package com.example.androidtutorial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidtutorial.R
import com.example.androidtutorial.models.Person

class EmployeeAdapter(
    private val employees: MutableList<Person>,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_ADD = 0
    private val TYPE_EMPLOYEE = 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD else TYPE_EMPLOYEE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD) {
            val view = inflater.inflate(R.layout.item_add_employee, parent, false)
            AddViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_employee, parent, false)
            EmployeeViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener { onAddClick() }
        } else if (holder is EmployeeViewHolder) {
            val employee = employees[position - 1]
            holder.tvName.text = employee.name
            holder.tvPosition.text = employee.position
        }
    }

    override fun getItemCount(): Int = employees.size + 1

    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)
    }

    class AddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
