package ru.lord.firebase_chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import ru.lord.firebase_chat.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var rcViewAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        setUpActionBar()

        val database = Firebase.database
        val chatRef = database.getReference("Chat_0")
        val messagesRef = chatRef.child("messages")
        val userRef = chatRef.child(auth.currentUser!!.uid)

        with(receiver = binding) {
            edMessage.addTextChangedListener {
                bSend.isEnabled = it?.isNotBlank() ?: false
            }
            bSend.setOnClickListener {
                messagesRef
                    .child(messagesRef.push().key ?: "bla-bla")
                    .setValue(
                        DatabaseMessage(
                            author = auth.currentUser?.displayName,
                            text = edMessage.text.toString().apply {
                                edMessage.text.clear()
                            }
                        )
                    )
            }
            bToEnd.setOnClickListener {
                rcView.smoothScrollToPosition(rcViewAdapter.lastIndex)
                bToEnd.isVisible = false
            }
        }
        initRcView(chatRef = chatRef)
        onChangeListener(messagesRef = messagesRef, userRef = userRef)
    }

    private fun initRcView(chatRef: DatabaseReference) = with(binding) {
        rcViewAdapter = MessageAdapter(user = auth.currentUser, dbRef = chatRef)
        with(receiver = rcView) {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
            }
            adapter = rcViewAdapter
            addItemDecoration(MessageDecoration())
            addOnScrollListener(
                OnRecyclerViewScrollListener {
                    bToEnd.isVisible = it &&
                        (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() < rcViewAdapter.lastIndex - 2
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sign_out) {
            googleSignInClient.signOut().addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.signOut()
                    launchActivityAndFinish(SignInActivity::class.java)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onChangeListener(messagesRef: DatabaseReference, userRef: DatabaseReference, first: Boolean = true) {
        fun onChatChangeListener(first: Boolean = false) {
            fun getRootValueEventListener(onUpdateListener: () -> Unit = {}) = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children
                        .map {
                            it.getValue(DatabaseMessage::class.java)?.toMessage(it.key!!)
                        }
                    if (list.isNotEmpty()) {
                        val childUpdates = hashMapOf<String, Any?>()
                        val deletedList = rcViewAdapter.list.filter { !list.contains(it) }
                        if (deletedList.isNotEmpty()) {
                            deletedList.forEach {
                                childUpdates["/${it.key}"] = null
                            }
                        }
                        val last = rcViewAdapter.list.lastOrNull { !deletedList.contains(it) }
                        val addedList: List<Message?> = last?.let {
                            list
                                .dropWhile {
                                    rcViewAdapter.list.isNotEmpty() && it?.key != last.key
                                }
                                .drop(1)
                        } ?: list
                        if (addedList.isNotEmpty()) {
                            addedList
                                .forEach { message ->
                                    childUpdates["/${message!!.key}"] =
                                        message.toDatabaseMessage().toMap()
                                }
                        }
                        userRef.updateChildren(childUpdates).addOnSuccessListener {
                            onUpdateListener()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }

            binding.syncMessage.isVisible = first
            with(receiver = messagesRef) {
                if (first) addListenerForSingleValueEvent(
                    getRootValueEventListener {
                        onChangeListener(messagesRef = messagesRef, userRef = userRef, first = false)
                        binding.syncMessage.isVisible = false
                    }
                )
                else addValueEventListener(getRootValueEventListener())
            }
        }

        fun getMyValueEventListener(onDataChangeListener: () -> Unit) = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                snapshot.children.forEach { s ->
                    s.getValue(DatabaseMessage::class.java)?.let { user ->
                        list.add(user.toMessage(s.key!!))
                    }
                }
                with(rcViewAdapter.list) {
                    clear()
                    addAll(elements = list)
                }
                onDataChangeListener()
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        with(userRef) {
            if (first) {
                addListenerForSingleValueEvent(
                    getMyValueEventListener {
                        onChatChangeListener(true)
                    }
                )
            } else {
                addValueEventListener(
                    getMyValueEventListener {
                        val needScroll =
                            (binding.rcView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == rcViewAdapter.lastIndex ||
                                rcViewAdapter.list.last().author == auth.currentUser?.displayName
                        rcViewAdapter.update()
                        if (needScroll) binding.rcView.smoothScrollToPosition(rcViewAdapter.lastIndex)
                    }
                )
                onChatChangeListener()
            }
        }
    }

    private fun setUpActionBar() {
        supportActionBar?.run {
            auth.currentUser?.let { firebaseUser ->
                thread(isDaemon = true) {
                    val dIcon = Picasso.get().load(firebaseUser.photoUrl).get().let { bMap ->
                        RoundedBitmapDrawableFactory.create(resources, bMap).apply {
                            isCircular = true
                        }
                    }
                    runOnUiThread {
                        setDisplayHomeAsUpEnabled(true)
                        setHomeAsUpIndicator(dIcon)
                        title = firebaseUser.displayName
                    }
                }
            }
        }
    }
}
