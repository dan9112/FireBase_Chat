package ru.lord.firebase_chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.lord.firebase_chat.databinding.UserListItemBinding

class UserAdapter(private val userName: String?) : ListAdapter<User, UserAdapter.ItemHolder>(ItemComparator()) {
    class ItemHolder(private val binding: UserListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User, othersMessage: Boolean) = with(receiver = binding) {
            with(receiver = message) {
                gravity = if (!othersMessage) Gravity.END else Gravity.START
                text = user.message
            }
            with(receiver = userName) {
                gravity = if (!othersMessage) Gravity.END else Gravity.START
                text = user.name
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem == newItem

        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context).let { context ->
            ItemHolder(UserListItemBinding.inflate(context, parent, false))
        }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) = getItem(position).let { user ->
        holder.bind(user = user, othersMessage = user.name != userName)
    }
}
