package ru.lord.firebase_chat

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import ru.lord.firebase_chat.databinding.UserListItemBinding

class MessageAdapter(private val userName: String?, private val dbRef: DatabaseReference) :
    ListAdapter<Message, MessageAdapter.ItemHolder>(ItemComparator()) {
    inner class ItemHolder(private val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, othersMessage: Boolean) = with(receiver = binding) {
            with(receiver = this.message) {
                gravity = if (!othersMessage) Gravity.END else Gravity.START
                text = message.text
            }
            with(receiver = userName) {
                gravity = if (!othersMessage) Gravity.END else Gravity.START
                text = message.author
            }
            with(receiver = container) {
                layoutParams = FrameLayout.LayoutParams(
                    layoutParams.width,
                    layoutParams.height,
                    if (!othersMessage) Gravity.END else Gravity.START
                )
                if (!othersMessage) setOnLongClickListener {
                    val popupMenu = PopupMenu(it.context, it)
                    popupMenu.inflate(R.menu.message_popup_menu)
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        if (menuItem.itemId == R.id.delete) {
                            dbRef.child(message.key).removeValue { error, _ ->
                                error?.run {
                                    Log.e(TAG, toString())
                                    Toast.makeText(it.context, "Ошибка при удалении!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            return@setOnMenuItemClickListener true
                        }
                        false
                    }
                    popupMenu.show()
                    true
                }
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem

        override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context).let { context ->
            ItemHolder(UserListItemBinding.inflate(context, parent, false))
        }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = getItem(position).let { user ->
        holder.bind(message = user, othersMessage = user.author != userName)
    }

    private companion object {
        const val TAG = "MessageAdapter"
    }
}
