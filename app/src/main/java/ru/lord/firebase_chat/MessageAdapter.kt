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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import ru.lord.firebase_chat.databinding.UserListItemBinding

class MessageAdapter(private val user: FirebaseUser?, private val dbRef: DatabaseReference) :
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
                setOnLongClickListener {
                    val popupMenu = PopupMenu(it.context, it)
                    popupMenu.inflate(R.menu.message_popup_menu)
                    popupMenu.menu.findItem(R.id.deleteAll).isVisible = !othersMessage
                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.deleteAll -> {
                                val childUpdates = hashMapOf<String, Any?>(
                                    "/messages/${message.key}" to null,
                                    "/${user?.uid}/${message.key}" to null
                                )

                                dbRef
                                    .updateChildren(childUpdates)
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, exception.toString())
                                        Toast.makeText(it.context, "Ошибка при удалении!", Toast.LENGTH_SHORT).show()
                                    }
                                return@setOnMenuItemClickListener true
                            }

                            R.id.delete -> {
                                dbRef
                                    .child(user!!.uid)
                                    .child(message.key)
                                    .removeValue()
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, exception.toString())
                                        Toast.makeText(it.context, "Ошибка при удалении!", Toast.LENGTH_SHORT).show()
                                    }
                                return@setOnMenuItemClickListener true
                            }

                            else -> false
                        }
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
        holder.bind(message = user, othersMessage = user.author != this.user?.displayName)
    }

    private companion object {
        const val TAG = "MessageAdapter"
    }
}
