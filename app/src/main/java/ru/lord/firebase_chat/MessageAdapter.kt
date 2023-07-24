package ru.lord.firebase_chat

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import ru.lord.firebase_chat.databinding.UserListItemBinding

class MessageAdapter(private val user: FirebaseUser?, private val dbRef: DatabaseReference) :
    RecyclerView.Adapter<MessageAdapter.ItemHolder>() {
    private val currentList = mutableListOf<Message>()
    val list = mutableListOf<Message>()

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

    val lastIndex
        get() = currentList.lastIndex

    fun update() {
        val diffCallback = ItemDiffUtilCallback(oldList = currentList, newList = list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        currentList.clear()
        currentList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    class ItemDiffUtilCallback(private val oldList: List<Message>, private val newList: List<Message>) :
        DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].key == newList[newItemPosition].key

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].author == newList[newItemPosition].author &&
                oldList[oldItemPosition].text == newList[newItemPosition].text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = currentList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = currentList[position].let { user ->
        holder.bind(message = user, othersMessage = user.author != this.user?.displayName)
    }

    private companion object {
        const val TAG = "ExtendedMessageAdapter"
    }
}
