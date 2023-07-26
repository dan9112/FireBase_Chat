package ru.lord.firebase_chat

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE

class OnRecyclerViewScrollListener(private val onScrollStateChanged: (Boolean) -> Unit) :
    RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        onScrollStateChanged(newState == SCROLL_STATE_IDLE)
    }
}
