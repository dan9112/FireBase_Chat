package ru.lord.firebase_chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
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
    private lateinit var rcViewAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        setUpActionBar()

        val database = Firebase.database
        val myRef = database.getReference("messages")

        with(receiver = binding) {
            edMessage.addTextChangedListener {
                bSend.isEnabled = it?.isNotBlank() ?: false
            }
            bSend.setOnClickListener {
                myRef
                    .child(myRef.push().key ?: "bla-bla")
                    .setValue(
                        User(
                            name = auth.currentUser?.displayName,
                            message = edMessage.text.toString().apply {
                                edMessage.text.clear()
                            }
                        )
                    )
            }
        }
        onChangeListener(dRef = myRef)
        initRcView()
    }

    private fun initRcView() = with(binding) {
        rcViewAdapter = UserAdapter(auth.currentUser?.displayName)
        with(receiver = rcView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = rcViewAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sign_out) {
            googleSignInClient.signOut().addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.signOut()
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onChangeListener(dRef: DatabaseReference) {
        dRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<User>()
                    snapshot.children.forEach { s ->
                        s.getValue(User::class.java)?.let { user ->
                            list.add(user)
                        }
                    }
                    rcViewAdapter.submitList(list)
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )
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
