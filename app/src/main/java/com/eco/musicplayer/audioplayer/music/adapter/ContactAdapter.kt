package com.eco.musicplayer.audioplayer.music.democontent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eco.musicplayer.audioplayer.music.R
import com.eco.musicplayer.audioplayer.music.models.contentprovider.Contact

class ContactAdapter(
    private val onStarClick: (Contact) -> Unit,
    private val onDeleteClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tvDisplayName)
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val btnStar: ImageButton = itemView.findViewById(R.id.btnStar)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(contact: Contact) {
            tvAvatar.text = contact.displayName.firstOrNull()?.toString()?.uppercase() ?: "?"

            tvDisplayName.text = contact.displayName

            if (!contact.phoneNumber.isNullOrEmpty()) {
                tvPhoneNumber.text = contact.phoneNumber
                tvPhoneNumber.visibility = View.VISIBLE
            } else {
                tvPhoneNumber.visibility = View.GONE
            }

            if (!contact.email.isNullOrEmpty()) {
                tvEmail.text = contact.email
                tvEmail.visibility = View.VISIBLE
            } else {
                tvEmail.visibility = View.GONE
            }

            val starIcon = if (contact.starred) {
                android.R.drawable.star_big_on
            } else {
                android.R.drawable.star_big_off
            }
            btnStar.setImageResource(starIcon)

            btnStar.setOnClickListener {
                onStarClick(contact)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(contact)
            }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}